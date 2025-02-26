package io.quarkus.artemis.core.deployment;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisDevServicesBuildTimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisUtil;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CuratedApplicationShutdownBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem.RunningDevService;
import io.quarkus.deployment.builditem.DevServicesSharedNetworkBuildItem;
import io.quarkus.deployment.builditem.DockerStatusBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.ContainerAddress;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.configuration.ConfigUtils;
import io.smallrye.config.NameIterator;

/**
 * Start a ActiveMQ Artemis broker if needed
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DevServicesArtemisProcessor {
    private static final Logger LOGGER = Logger.getLogger(DevServicesArtemisProcessor.class);
    private static final String QUARKUS_ARTEMIS_BASE = "quarkus.artemis.";
    private static final String QUARKUS_ARTEMIS_NAMED_BASE_TEMPLATE = "quarkus.artemis.%s.";

    /**
     * Label to add to shared Dev Service for ActiveMQ Artemis running in containers.
     * This allows other applications to discover the running service and use it instead of starting a new instance.
     */
    static final String DEV_SERVICE_LABEL = "quarkus-dev-service-artemis";
    static final int ARTEMIS_PORT = 61616;
    static final int ARTEMIS_WEB_UI_PORT = 8161;

    private static final ContainerLocator artemisContainerLocator = new ContainerLocator(DEV_SERVICE_LABEL, ARTEMIS_PORT);

    static final ConcurrentHashMap<String, RunningDevService> devServices = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<String, ArtemisDevServiceCfg> cfgs = new ConcurrentHashMap<>();
    static volatile boolean first = true;

    @SuppressWarnings("unused")
    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = GlobalDevServicesConfig.Enabled.class)
    public List<DevServicesResultBuildItem> startArtemisDevService(
            DockerStatusBuildItem dockerStatusBuildItem,
            LaunchModeBuildItem launchMode,
            ArtemisBootstrappedBuildItem bootstrap,
            ShadowRuntimeConfigs shadowRunTimeConfigs,
            ArtemisBuildTimeConfigs buildConfigs,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            CuratedApplicationShutdownBuildItem closeBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem,
            GlobalDevServicesConfig devServicesConfig) {
        ArrayList<DevServicesResultBuildItem> results = new ArrayList<>();
        for (String name : bootstrap.getConfigurationNames()) {
            String propertyName = name;
            for (String rawName : ConfigProvider.getConfig().getPropertyNames()) {
                if (rawName.contains(name)) {
                    rawName = rawName.substring("quarkus.artemis.".length());
                    NameIterator nameIterator = new NameIterator(rawName);
                    if (nameIterator.nextSegmentEquals(name)) {
                        int end = nameIterator.getNextEnd();
                        if (rawName.charAt(0) == '"' && rawName.charAt(end - 1) == '"') {
                            propertyName = rawName.substring(0, end);
                            break;
                        }
                    }
                }
            }
            ArtemisBuildTimeConfig buildTimeConfig = buildConfigs.configs().get(name);
            boolean isUrlEmpty = shadowRunTimeConfigs.isUrlEmpty(name);
            if (!shadowRunTimeConfigs.getNames().contains(name) && buildTimeConfig.isEmpty()) {
                LOGGER.debugf(
                        "Not starting dev services for ActiveMQ Artemis and configuration %s, as its configuration is empty.",
                        name);
                continue;
            }
            ArtemisDevServiceCfg configuration = getConfiguration(
                    buildTimeConfig,
                    name,
                    isUrlEmpty);
            DevServicesResultBuildItem result = start(
                    configuration,
                    propertyName,
                    dockerStatusBuildItem,
                    launchMode,
                    consoleInstalledBuildItem,
                    closeBuildItem,
                    loggingSetupBuildItem,
                    devServicesConfig);
            if (result != null) {
                results.add(result);
            }
        }

        return results;
    }

    private static DevServicesResultBuildItem start(
            ArtemisDevServiceCfg configuration,
            String name,
            DockerStatusBuildItem dockerStatusBuildItem,
            LaunchModeBuildItem launchMode,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            CuratedApplicationShutdownBuildItem closeBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem,
            GlobalDevServicesConfig devServicesConfig) {
        if (devServices.get(name) != null && configuration != null) {
            boolean shouldShutdownTheBroker = !configuration.equals(cfgs.get(name));
            if (!shouldShutdownTheBroker) {
                return devServices.get(name).toBuildItem();
            }
            shutdownBroker(name);
            cfgs.clear();
        }

        if (configuration != null) {
            try (StartupLogCompressor compressor = new StartupLogCompressor(
                    (launchMode.isTest() ? "(test) " : "") + "ActiveMQ Artemis Dev Services Starting:",
                    consoleInstalledBuildItem, loggingSetupBuildItem)) {
                try {
                    // devServices
                    RunningDevService service = startArtemis(
                            name,
                            dockerStatusBuildItem,
                            configuration,
                            launchMode,
                            devServicesConfig.timeout);
                    if (service != null) {
                        devServices.put(name, service);
                    }
                    if (devServices.get(name) == null) {
                        compressor.closeAndDumpCaptured();
                    }
                } catch (Throwable t) {
                    compressor.closeAndDumpCaptured();
                    throw t instanceof RuntimeException runtimeException ? runtimeException : new RuntimeException(t);
                }
            }
        }

        if (devServices.get(name) == null) {
            return null;
        }

        // Configure the watch dog
        if (first) {
            first = false;
            Runnable closeTask = () -> {
                for (String serviceName : devServices.keySet()) {
                    shutdownBroker(serviceName);
                }
                first = true;
                devServices.clear();
                cfgs.clear();
            };
            closeBuildItem.addCloseTask(closeTask, true);
        }
        cfgs.put(name, Objects.requireNonNull(configuration));

        if (devServices.get(name).isOwner()) {
            LOGGER.infof("Dev Services for ActiveMQ Artemis and named configuration %s started on %s",
                    name, getArtemisUrl(name));

            if (getArtemisWebUiUrl(name) != null) {
                LOGGER.infof("Artemis Management Console for named configuration %s is available at %s",
                        name, getArtemisWebUiUrl(name));
            }
        }
        return devServices.get(name).toBuildItem();
    }

    private static String getArtemisUrl(String name) {
        return devServices.get(name).getConfig().get(getUrlPropertyName(name));
    }

    private static String getArtemisWebUiUrl(String name) {
        return devServices.get(name).getConfig().get(getWebUiUrlPropertyName(name));
    }

    private static String getUrlPropertyName(String name) {
        return getArtemisPropertyBase(name) + "url";
    }

    private static String getWebUiUrlPropertyName(String name) {
        return getArtemisPropertyBase(name) + "web-ui-url";
    }

    private static String getArtemisPropertyBase(String name) {
        if (Objects.equals(ArtemisUtil.DEFAULT_CONFIG_NAME, name)) {
            return QUARKUS_ARTEMIS_BASE;
        } else {
            return String.format(QUARKUS_ARTEMIS_NAMED_BASE_TEMPLATE, name);
        }
    }

    private static void shutdownBroker(String name) {
        if (devServices.get(name) != null) {
            try {
                devServices.get(name).close();
            } catch (Throwable t) {
                LOGGER.error("Failed to stop the ActiveMQ Artemis broker", t);
            } finally {
                devServices.remove(name);
            }
        }
    }

    private static RunningDevService startArtemis(
            String name,
            DockerStatusBuildItem dockerStatusBuildItem,
            ArtemisDevServiceCfg config,
            LaunchModeBuildItem launchMode,
            Optional<Duration> timeout) {
        if (!config.devServicesEnabled) {
            // explicitly disabled
            LOGGER.debugf(
                    "Not starting dev services for ActiveMQ Artemis and configuration %s, as it has been disabled in the config.",
                    name);
            return null;
        }

        // Check if quarkus.artemis.url is set
        String urlPropertyName = getUrlPropertyName(name);
        if (ConfigUtils.isPropertyPresent(urlPropertyName)) {
            LOGGER.debugf(
                    "Not starting dev services for ActiveMQ Artemis and configuration %s, the quarkus.artemis.url is configured.",
                    name);
            return null;
        }

        if (!dockerStatusBuildItem.isDockerAvailable()) {
            LOGGER.warn(
                    "Docker isn't working, please configure the ActiveMQ Artemis Url property (quarkus.artemis.url).");
            return null;
        }

        final Optional<ContainerAddress> maybeContainerAddress = artemisContainerLocator.locateContainer(config.serviceName,
                config.shared,
                launchMode.getLaunchMode());

        // Starting the broker
        String containerName = "ActiveMQ-Artemis " + name;
        Supplier<RunningDevService> defaultArtemisBrokerSupplier = () -> {
            ArtemisContainer container = new ArtemisContainer(
                    DockerImageName.parse(config.imageName),
                    config.fixedExposedPort,
                    config.webUiPort,
                    config.user,
                    config.password,
                    mergeExtraArgs(config.defaultExtraArgs, config.extraArgs));

            ConfigureUtil.configureSharedNetwork(container, "artemis");

            if (config.serviceName != null) {
                container.withLabel(DevServicesArtemisProcessor.DEV_SERVICE_LABEL, config.serviceName);
            }

            timeout.ifPresent(container::withStartupTimeout);

            container.start();
            return new RunningDevService(
                    containerName,
                    container.getContainerId(),
                    container::close,
                    Map.of(
                            urlPropertyName, String.format("tcp://%s:%d", container.getHost(), container.getPort()),
                            getWebUiUrlPropertyName(name), String.format("http://%s:%d", container.getHost(),
                                    container.getMappedPort(ARTEMIS_WEB_UI_PORT))));
        };

        return maybeContainerAddress
                .map(containerAddress -> new RunningDevService(
                        containerName,
                        containerAddress.getId(),
                        null,
                        Map.of(urlPropertyName, String.format("tcp://%s", containerAddress.getUrl()))))
                .orElseGet(defaultArtemisBrokerSupplier);
    }

    private ArtemisDevServiceCfg getConfiguration(ArtemisBuildTimeConfig config, String name, boolean isUrlEmpty) {
        if (config.getDevservices() != null) {
            return new ArtemisDevServiceCfg(config, name, isUrlEmpty);
        }
        return null;
    }

    private static String mergeExtraArgs(String defaultExtraArgs, String extraArgs) {
        List<String> paramsToAdd = Arrays.stream(defaultExtraArgs.split(" "))
                .filter(da -> !extraArgs.contains(da)).toList();

        return String.join(" ", paramsToAdd) + (paramsToAdd.isEmpty() ? "" : " ") + extraArgs;
    }

    private static final class ArtemisDevServiceCfg {
        private final boolean devServicesEnabled;
        private final String imageName;
        private final Integer fixedExposedPort;
        private final Integer webUiPort;
        private final boolean shared;
        private final String serviceName;
        private final String user;
        private final String password;
        private final String extraArgs;
        private final String defaultExtraArgs;

        public ArtemisDevServiceCfg(ArtemisBuildTimeConfig config, String name, boolean isUrlEmpty) {
            ArtemisDevServicesBuildTimeConfig devServicesConfig = config.getDevservices();
            this.devServicesEnabled = devServicesConfig.enabled().orElse(isUrlEmpty);
            this.imageName = devServicesConfig.getImageName();
            this.fixedExposedPort = devServicesConfig.getPort();
            this.webUiPort = devServicesConfig.getWebUiPort();
            this.shared = devServicesConfig.isShared();
            this.serviceName = devServicesConfig.getServiceName() + "-" + name;
            this.user = devServicesConfig.getUser();
            this.password = devServicesConfig.getPassword();
            this.extraArgs = devServicesConfig.getExtraArgs();
            this.defaultExtraArgs = devServicesConfig.getDefaultExtraArgs();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ArtemisDevServiceCfg that = (ArtemisDevServiceCfg) o;
            return devServicesEnabled == that.devServicesEnabled && Objects.equals(imageName, that.imageName)
                    && Objects.equals(fixedExposedPort, that.fixedExposedPort);
        }

        @Override
        public int hashCode() {
            return Objects.hash(devServicesEnabled, imageName, fixedExposedPort);
        }
    }

    /**
     * Container configuring and starting the Artemis broker.
     */
    private static final class ArtemisContainer extends GenericContainer<ArtemisContainer> {

        private final int port;
        private final int webUiPort;

        private ArtemisContainer(DockerImageName dockerImageName, int fixedExposedPort, int webUiPort, String user,
                String password, String extra) {
            super(dockerImageName);
            this.port = fixedExposedPort;
            this.webUiPort = webUiPort;

            withNetwork(Network.SHARED)
                    .withExposedPorts(ARTEMIS_PORT, ARTEMIS_WEB_UI_PORT)
                    .withEnv("AMQ_USER", user)
                    .withEnv("AMQ_PASSWORD", password)
                    .withEnv("AMQ_EXTRA_ARGS", extra)
                    .waitingFor(Wait.forLogMessage(".*AMQ241004.*", 1)); // Artemis console available.
        }

        @Override
        protected void configure() {
            super.configure();
            if (port > 0) {
                addFixedExposedPort(port, ARTEMIS_PORT);
            }
            if (webUiPort > 0) {
                addFixedExposedPort(webUiPort, ARTEMIS_WEB_UI_PORT);
            }
        }

        public int getPort() {
            return getMappedPort(ARTEMIS_PORT);
        }
    }
}
