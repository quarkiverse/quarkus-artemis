package io.quarkus.artemis.core.deployment;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

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

/**
 * Start a ActiveMQ Artemis broker if needed
 */
public class DevServicesArtemisProcessor {
    private static final Logger LOGGER = Logger.getLogger(DevServicesArtemisProcessor.class);
    private static final String QUARKUS_ARTEMIS_URL = "quarkus.artemis.url";

    /**
     * Label to add to shared Dev Service for ActiveMQ Artemis running in containers.
     * This allows other applications to discover the running service and use it instead of starting a new instance.
     */
    static final String DEV_SERVICE_LABEL = "quarkus-dev-service-artemis";
    static final int ARTEMIS_PORT = 61616;

    private static final ContainerLocator artemisContainerLocator = new ContainerLocator(DEV_SERVICE_LABEL, ARTEMIS_PORT);

    static volatile RunningDevService devService;
    static volatile ArtemisDevServiceCfg cfg;
    static volatile boolean first = true;

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = GlobalDevServicesConfig.Enabled.class)
    public DevServicesResultBuildItem startArtemisDevService(
            DockerStatusBuildItem dockerStatusBuildItem,
            LaunchModeBuildItem launchMode,
            ArtemisBuildTimeConfig artemisBuildTimeConfig,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            CuratedApplicationShutdownBuildItem closeBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem, GlobalDevServicesConfig devServicesConfig) {

        ArtemisDevServiceCfg configuration = getConfiguration(artemisBuildTimeConfig);

        if (devService != null) {
            boolean shouldShutdownTheBroker = !configuration.equals(cfg);
            if (!shouldShutdownTheBroker) {
                return devService.toBuildItem();
            }
            shutdownBroker();
            cfg = null;
        }

        StartupLogCompressor compressor = new StartupLogCompressor(
                (launchMode.isTest() ? "(test) " : "") + "ActiveMQ Artemis Dev Services Starting:",
                consoleInstalledBuildItem, loggingSetupBuildItem);
        try {
            devService = startArtemis(dockerStatusBuildItem, configuration, launchMode,
                    !devServicesSharedNetworkBuildItem.isEmpty(),
                    devServicesConfig.timeout);
            if (devService == null) {
                compressor.closeAndDumpCaptured();
            } else {
                compressor.close();
            }
        } catch (Throwable t) {
            compressor.closeAndDumpCaptured();
            throw new RuntimeException(t);
        }

        if (devService == null) {
            return null;
        }

        // Configure the watch dog
        if (first) {
            first = false;
            Runnable closeTask = () -> {
                if (devService != null) {
                    shutdownBroker();
                }
                first = true;
                devService = null;
                cfg = null;
            };
            closeBuildItem.addCloseTask(closeTask, true);
        }
        cfg = configuration;

        if (devService.isOwner()) {
            LOGGER.infof("Dev Services for ActiveMQ Artemis started on %s", getArtemisUrl());
        }

        return devService.toBuildItem();
    }

    public static String getArtemisUrl() {
        return devService.getConfig().get(QUARKUS_ARTEMIS_URL);
    }

    private void shutdownBroker() {
        if (devService != null) {
            try {
                devService.close();
            } catch (Throwable e) {
                LOGGER.error("Failed to stop the ActiveMQ Artemis broker", e);
            } finally {
                devService = null;
            }
        }
    }

    private RunningDevService startArtemis(DockerStatusBuildItem dockerStatusBuildItem, ArtemisDevServiceCfg config,
            LaunchModeBuildItem launchMode, boolean useSharedNetwork, Optional<Duration> timeout) {
        if (!config.devServicesEnabled) {
            // explicitly disabled
            LOGGER.debug("Not starting dev services for ActiveMQ Artemis, as it has been disabled in the config.");
            return null;
        }

        // Check if quarkus.artemis.url is set
        if (ConfigUtils.isPropertyPresent(QUARKUS_ARTEMIS_URL)) {
            LOGGER.debug("Not starting dev services for ActiveMQ Artemis, the quarkus.artemis.url is configured.");
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
        final Supplier<RunningDevService> defaultArtemisBrokerSupplier = () -> {
            GenericContainer<?> container = new GenericContainer<>(config.imageName)
                    .withExposedPorts(ARTEMIS_PORT)
                    .withEnv("AMQ_USER", config.user)
                    .withEnv("AMQ_PASSWORD", config.password)
                    .waitingFor(Wait.forLogMessage(".* Apache ActiveMQ Artemis Message Broker .*", 1));

            ConfigureUtil.configureSharedNetwork(container, "artemis");

            if (config.serviceName != null) {
                container.withLabel(DevServicesArtemisProcessor.DEV_SERVICE_LABEL, config.serviceName);
            }

            timeout.ifPresent(container::withStartupTimeout);

            container.start();
            return new RunningDevService("ActiveMQ-Artemis",
                    container.getContainerId(),
                    container::close,
                    QUARKUS_ARTEMIS_URL, String.format("tcp://localhost:%d", container.getMappedPort(ARTEMIS_PORT)));
        };

        return maybeContainerAddress
                .map(containerAddress -> new RunningDevService("ActiveMQ-Artemis",
                        containerAddress.getId(),
                        null,
                        QUARKUS_ARTEMIS_URL, containerAddress.getUrl()))
                .orElseGet(defaultArtemisBrokerSupplier);
    }

    private ArtemisDevServiceCfg getConfiguration(ArtemisBuildTimeConfig cfg) {
        ArtemisDevServicesBuildTimeConfig devServicesConfig = cfg.devservices;
        return new ArtemisDevServiceCfg(devServicesConfig);
    }

    private static final class ArtemisDevServiceCfg {
        private final boolean devServicesEnabled;
        private final String imageName;
        private final Integer fixedExposedPort;
        private final boolean shared;
        private final String serviceName;

        private final String user;

        private final String password;

        public ArtemisDevServiceCfg(ArtemisDevServicesBuildTimeConfig config) {
            this.devServicesEnabled = config.enabled.orElse(true);
            this.imageName = config.imageName;
            this.fixedExposedPort = config.port.orElse(0);
            this.shared = config.shared;
            this.serviceName = config.serviceName;
            this.user = config.user;
            this.password = config.password;
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
}
