package io.quarkus.artemis.core.deployment;

import static io.quarkus.devservices.common.ConfigureUtil.configureSharedServiceLabel;
import static io.quarkus.devservices.common.ContainerLocator.locateContainerWithLabels;

import java.time.Duration;
import java.util.*;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisConstants;
import io.quarkus.artemis.core.runtime.ArtemisDevServicesBuildTimeConfig;
import io.quarkus.deployment.IsDevServicesSupportedByLaunchMode;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.*;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.devservices.common.ComposeLocator;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.configuration.ConfigUtils;
import io.smallrye.config.NameIterator;

/**
 * Start a ActiveMQ Artemis broker if needed
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@BuildSteps(onlyIf = { IsDevServicesSupportedByLaunchMode.class, DevServicesConfig.Enabled.class })
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

    private static final ContainerLocator artemisContainerLocator = locateContainerWithLabels(ARTEMIS_PORT,
            DEV_SERVICE_LABEL);

    @BuildStep
    void startArtemisDevService(
            DockerStatusBuildItem dockerStatusBuildItem,
            DevServicesComposeProjectBuildItem composeProjectBuildItem,
            LaunchModeBuildItem launchMode,
            ArtemisBootstrappedBuildItem bootstrap,
            ShadowRuntimeConfigs shadowRunTimeConfigs,
            ArtemisBuildTimeConfigs buildConfigs,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            DevServicesConfig devServicesConfig,
            BuildProducer<DevServicesResultBuildItem> devServicesResult,
            BuildProducer<CardPageBuildItem> cardPageProducer) {

        CardPageBuildItem cardPage = new CardPageBuildItem();

        for (String name : bootstrap.getConfigurationNames()) {
            String propertyName = name;
            for (String rawName : ConfigProvider.getConfig().getPropertyNames()) {
                if (rawName.contains(name)) {
                    rawName = rawName.substring(QUARKUS_ARTEMIS_BASE.length());
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

            String feature = "ActiveMQ-Artemis " + name;

            ArtemisBuildTimeConfig buildTimeConfig = buildConfigs.configs().get(name);
            boolean isUrlEmpty = shadowRunTimeConfigs.isUrlEmpty(name);
            if (!shadowRunTimeConfigs.getNames().contains(name) && buildTimeConfig.isEmpty()) {
                LOGGER.debugf(
                        "Not starting dev services for ActiveMQ Artemis and configuration %s, as its configuration is empty.",
                        name);
                continue;
            }

            ArtemisDevServiceCfg configuration = getConfiguration(buildTimeConfig, name, isUrlEmpty);
            if (configuration == null || !configuration.devServicesEnabled) {
                if (configuration != null) {
                    LOGGER.debugf(
                            "Not starting dev services for ActiveMQ Artemis and configuration %s, as it has been disabled in the config.",
                            name);
                }
                continue;
            }

            String urlPropertyName = getUrlPropertyName(propertyName);
            String webUiUrlPropertyName = getWebUiUrlPropertyName(propertyName);

            if (ConfigUtils.isPropertyPresent(urlPropertyName)) {
                LOGGER.debugf(
                        "Not starting dev services for ActiveMQ Artemis and configuration %s, the quarkus.artemis.url is configured.",
                        name);
                continue;
            }

            if (!dockerStatusBuildItem.isContainerRuntimeAvailable()) {
                LOGGER.warn(
                        "Docker isn't working, please configure the ActiveMQ Artemis Url property (quarkus.artemis.url).");
                continue;
            }

            boolean useSharedNetwork = DevServicesSharedNetworkBuildItem.isSharedNetworkRequired(devServicesConfig,
                    devServicesSharedNetworkBuildItem);

            DevServicesResultBuildItem discovered = discoverRunningService(composeProjectBuildItem, name, configuration,
                    urlPropertyName, launchMode.getLaunchMode(), useSharedNetwork, feature);
            if (discovered != null) {
                devServicesResult.produce(discovered);
            } else {
                Optional<Duration> timeout = devServicesConfig.timeout();
                devServicesResult.produce(DevServicesResultBuildItem.<ArtemisContainer> owned()
                        .feature(feature)
                        .serviceName(configuration.serviceName)
                        .serviceConfig(configuration)
                        .startable(() -> {
                            ArtemisContainer container = new ArtemisContainer(
                                    DockerImageName.parse(configuration.imageName),
                                    configuration.fixedExposedPort,
                                    configuration.webUiPort,
                                    composeProjectBuildItem.getDefaultNetworkId(),
                                    useSharedNetwork,
                                    configuration.user,
                                    configuration.password,
                                    mergeExtraArgs(configuration.defaultExtraArgs, configuration.extraArgs));
                            timeout.ifPresent(container::withStartupTimeout);
                            return container.withSharedServiceLabel(launchMode.getLaunchMode(), configuration.serviceName);
                        })
                        .configProvider(Map.of(
                                urlPropertyName,
                                container -> String.format("tcp://%s:%d", container.getHost(), container.getPort()),
                                webUiUrlPropertyName,
                                container -> String.format("http://%s:%d", container.getHost(), container.getWebUiPort())))
                        .build());
            }

            cardPage.addPage(Page.externalPageBuilder(name + " web UI")
                    .dynamicUrlJsonRPCMethodName("devui-dev-services:devServicesConfig",
                            Map.of("name", feature, "configKey", webUiUrlPropertyName))
                    .doNotEmbed()
                    .isHtmlContent()
                    .icon("font-awesome-solid:binoculars"));
            cardPage.addPage(Page.externalPageBuilder(name + " broker URL")
                    .dynamicUrlJsonRPCMethodName("devui-dev-services:devServicesConfig",
                            Map.of("name", feature, "configKey", urlPropertyName))
                    .doNotEmbed()
                    .isHtmlContent()
                    .staticLabel(urlPropertyName)
                    .icon("font-awesome-solid:plug"));
        }
        cardPageProducer.produce(cardPage);
    }

    private DevServicesResultBuildItem discoverRunningService(
            DevServicesComposeProjectBuildItem composeProjectBuildItem,
            String name,
            ArtemisDevServiceCfg config,
            String urlPropertyName,
            LaunchMode launchMode,
            boolean useSharedNetwork, String feature) {
        return artemisContainerLocator.locateContainer(config.serviceName, config.shared, launchMode)
                .or(() -> ComposeLocator.locateContainer(composeProjectBuildItem,
                        List.of(config.imageName, "artemis"),
                        ARTEMIS_PORT, launchMode, useSharedNetwork))
                .map(containerAddress -> DevServicesResultBuildItem.discovered()
                        .feature(feature)
                        .containerId(containerAddress.getId())
                        .config(Map.of(urlPropertyName,
                                String.format("tcp://%s:%d",
                                        containerAddress.getHost(), containerAddress.getPort())))
                        .build())
                .orElse(null);
    }

    private static String getUrlPropertyName(String name) {
        return getArtemisPropertyBase(name) + "url";
    }

    private static String getWebUiUrlPropertyName(String name) {
        return getArtemisPropertyBase(name) + "web-ui-url";
    }

    private static String getArtemisPropertyBase(String name) {
        if (Objects.equals(ArtemisConstants.DEFAULT_CONFIG_NAME, name)) {
            return QUARKUS_ARTEMIS_BASE;
        } else {
            return String.format(QUARKUS_ARTEMIS_NAMED_BASE_TEMPLATE, name);
        }
    }

    private static ArtemisDevServiceCfg getConfiguration(ArtemisBuildTimeConfig config, String name, boolean isUrlEmpty) {
        if (config.getDevservices() != null) {
            return new ArtemisDevServiceCfg(config, name, isUrlEmpty);
        }
        return null;
    }

    private static String mergeExtraArgs(String defaultExtraArgs, String extraArgs) {
        List<String> parsedDefaultArgs = Arrays.asList(defaultExtraArgs.split(" "));
        List<String> duplicatedParams = parsedDefaultArgs.stream().filter(extraArgs::contains).toList();
        if (!duplicatedParams.isEmpty()) {
            LOGGER.warnf("parameters %s are set in both default-extra-args and extra-args", duplicatedParams);
        }
        List<String> paramsToAdd = parsedDefaultArgs.stream().filter(p -> !duplicatedParams.contains(p)).toList();
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
    private static final class ArtemisContainer extends GenericContainer<ArtemisContainer>
            implements Startable {

        private final int fixedExposedPort;
        private final int fixedWebUiPort;
        private final boolean useSharedNetwork;
        private final String hostName;

        private ArtemisContainer(DockerImageName dockerImageName, int fixedExposedPort, int fixedWebUiPort,
                String defaultNetworkId, boolean useSharedNetwork, String user, String password, String extra) {
            super(dockerImageName);
            this.fixedExposedPort = fixedExposedPort;
            this.fixedWebUiPort = fixedWebUiPort;
            this.useSharedNetwork = useSharedNetwork;
            this.hostName = ConfigureUtil.configureNetwork(this, defaultNetworkId, useSharedNetwork, "artemis");
            withEnv("AMQ_USER", user)
                    .withEnv("AMQ_PASSWORD", password)
                    .withEnv("AMQ_EXTRA_ARGS", extra)
                    .waitingFor(Wait.forLogMessage(".*AMQ241004.*", 1)); // Artemis console available.
        }

        public ArtemisContainer withSharedServiceLabel(LaunchMode launchMode, String serviceName) {
            return configureSharedServiceLabel(this, launchMode, DEV_SERVICE_LABEL, serviceName);
        }

        @Override
        protected void configure() {
            super.configure();
            if (useSharedNetwork) {
                return;
            }
            if (fixedExposedPort > 0) {
                addFixedExposedPort(fixedExposedPort, ARTEMIS_PORT);
            } else {
                addExposedPort(ARTEMIS_PORT);
            }
            if (fixedWebUiPort > 0) {
                addFixedExposedPort(fixedWebUiPort, ARTEMIS_WEB_UI_PORT);
            } else {
                addExposedPort(ARTEMIS_WEB_UI_PORT);
            }
        }

        public int getPort() {
            if (useSharedNetwork) {
                return ARTEMIS_PORT;
            }
            if (fixedExposedPort > 0) {
                return fixedExposedPort;
            }
            return getMappedPort(ARTEMIS_PORT);
        }

        public int getWebUiPort() {
            if (useSharedNetwork) {
                return ARTEMIS_WEB_UI_PORT;
            }
            if (fixedWebUiPort > 0) {
                return fixedWebUiPort;
            }
            return getMappedPort(ARTEMIS_WEB_UI_PORT);
        }

        @Override
        public String getHost() {
            return useSharedNetwork ? hostName : super.getHost();
        }

        @Override
        public String getConnectionInfo() {
            return getHost() + ":" + getPort();
        }

        @Override
        public void close() {
            super.close();
        }
    }
}
