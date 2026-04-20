package io.quarkus.artemis.jms.ra.deployment;

import static io.quarkus.artemis.core.deployment.DevServicesArtemisProcessor.*;
import static io.quarkus.devservices.common.ConfigureUtil.configureSharedServiceLabel;
import static io.quarkus.devservices.common.ContainerLocator.locateContainerWithLabels;

import java.time.Duration;
import java.util.*;

import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.quarkiverse.ironjacamar.runtime.IronJacamarBuildtimeConfig;
import io.quarkus.artemis.jms.ra.runtime.ArtemisDevServicesBuildTimeConfig;
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

/**
 * Start a ActiveMQ Artemis broker if needed
 */
@BuildSteps(onlyIf = { IsDevServicesSupportedByLaunchMode.class, DevServicesConfig.Enabled.class })
public class DevServicesArtemisProcessor {
    private static final Logger LOGGER = Logger.getLogger(DevServicesArtemisProcessor.class);

    public static final String DEFAULT_CONFIG_NAME = "<default>";

    private static final String QUARKUS_ARTEMIS_URL = "quarkus.ironjacamar.ra.config.connection-parameters";

    private static final String QUARKUS_ARTEMIS_NAMED_URL_TEMPLATE = "quarkus.ironjacamar.%s.ra.config.connection-parameters";

    /**
     * Label to add to shared Dev Service for ActiveMQ Artemis running in containers.
     * This allows other applications to discover the running service and use it instead of starting a new instance.
     */
    static final String DEV_SERVICE_LABEL = "quarkus-dev-service-ironjacamar";

    static final int ARTEMIS_PORT = 61616;

    private static final ContainerLocator artemisContainerLocator = locateContainerWithLabels(ARTEMIS_PORT, DEV_SERVICE_LABEL);

    @BuildStep
    public void startArtemisContainers(
            LaunchModeBuildItem launchMode,
            DockerStatusBuildItem dockerStatusBuildItem,
            DevServicesComposeProjectBuildItem composeProjectBuildItem,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            IronJacamarBuildtimeConfig buildConfig,
            ShadowIronJacamarRuntimeConfig runtimeConfig,
            ArtemisDevServicesBuildTimeConfig devServicesBuildTimeConfig,
            BuildProducer<DevServicesResultBuildItem> devServicesResult,
            DevServicesConfig devServicesConfig,
            BuildProducer<CardPageBuildItem> cardPageProducer) {

        CardPageBuildItem cardPage = new CardPageBuildItem();

        for (String name : buildConfig.resourceAdapters().keySet()) {
            boolean isUrlEmpty = runtimeConfig.resourceAdapters().get(name).ra().config().connectionParameters().isEmpty();
            boolean useSharedNetwork = DevServicesSharedNetworkBuildItem.isSharedNetworkRequired(devServicesConfig,
                    devServicesSharedNetworkBuildItem);
            String feature = "ActiveMQ-Artemis " + name;

            ArtemisDevServiceCfg configuration = getConfiguration(devServicesBuildTimeConfig, name, isUrlEmpty);
            if (configuration == null || !configuration.devServicesEnabled) {
                if (configuration != null) {
                    LOGGER.debugf(
                            "Not starting dev services for ActiveMQ Artemis and configuration %s, as it has been disabled in the config.",
                            name);
                }
                continue;
            }

            String urlPropertyName = getUrlPropertyName(name);
            String webUiUrlPropertyName = getWebUiUrlPropertyName(name);

            if (ConfigUtils.isPropertyPresent(urlPropertyName)) {
                LOGGER.debugf(
                        "Not starting dev services for ActiveMQ Artemis and configuration %s, the quarkus.ironjacamar.ra.config.connection-parameters is configured.",
                        name);
                continue;
            }

            if (!dockerStatusBuildItem.isContainerRuntimeAvailable()) {
                LOGGER.warn(
                        "Docker isn't working, please configure the connection parameters property (quarkus.ironjacamar.ra.config.connection-parameters).");
                continue;
            }

            DevServicesResultBuildItem discovered = discoverRunningService(composeProjectBuildItem, feature, configuration,
                    urlPropertyName, launchMode.getLaunchMode(), useSharedNetwork);
            if (discovered != null) {
                devServicesResult.produce(discovered);
                continue;
            }

            Optional<Duration> timeout = devServicesConfig.timeout();
            devServicesResult.produce(DevServicesResultBuildItem.<ArtemisContainer> owned()
                    .feature(feature)
                    .serviceName(configuration.serviceName)
                    .serviceConfig(configuration)
                    .startable(() -> {
                        ArtemisContainer container = new ArtemisContainer(
                                DockerImageName.parse(configuration.imageName),
                                configuration.fixedExposedPort,
                                composeProjectBuildItem.getDefaultNetworkId(),
                                useSharedNetwork,
                                configuration.webUiPort,
                                configuration.user,
                                configuration.password,
                                configuration.extraArgs);
                        timeout.ifPresent(container::withStartupTimeout);
                        return container.withReuse(configuration.reuse)
                                .withSharedServiceLabel(launchMode.getLaunchMode(), configuration.serviceName);
                    })
                    .configProvider(Map.of(
                            urlPropertyName,
                            container -> String.format("host=%s;port=%d;protocols=CORE", container.getHost(),
                                    container.getPort()),
                            webUiUrlPropertyName,
                            container -> String.format("http://%s:%d", container.getHost(),
                                    container.getMappedPort(ARTEMIS_WEB_UI_PORT))))
                    .build());

            cardPage.addPage(Page.externalPageBuilder(name + " web UI")
                    .dynamicUrlJsonRPCMethodName("devui-dev-services:devServicesConfig",
                            Map.of("name", feature, "configKey", webUiUrlPropertyName))
                    .doNotEmbed()
                    .isHtmlContent()
                    .icon("font-awesome-solid:binoculars"));
        }
        cardPageProducer.produce(cardPage);
    }

    private DevServicesResultBuildItem discoverRunningService(
            DevServicesComposeProjectBuildItem composeProjectBuildItem,
            String feature,
            ArtemisDevServiceCfg config,
            String urlPropertyName,
            LaunchMode launchMode,
            boolean useSharedNetwork) {
        return artemisContainerLocator.locateContainer(config.serviceName, config.shared, launchMode)
                .or(() -> ComposeLocator.locateContainer(composeProjectBuildItem,
                        List.of(config.imageName, "artemis"),
                        ARTEMIS_PORT, launchMode, useSharedNetwork))
                .map(containerAddress -> DevServicesResultBuildItem.discovered()
                        .feature(feature)
                        .containerId(containerAddress.getId())
                        .config(Map.of(urlPropertyName,
                                String.format("host=%s;port=%d;protocols=CORE",
                                        containerAddress.getHost(), containerAddress.getPort())))
                        .build())
                .orElse(null);
    }

    private static String getUrlPropertyName(String name) {
        if (Objects.equals(DEFAULT_CONFIG_NAME, name)) {
            return QUARKUS_ARTEMIS_URL;
        } else {
            return String.format(QUARKUS_ARTEMIS_NAMED_URL_TEMPLATE, name);
        }
    }

    private ArtemisDevServiceCfg getConfiguration(ArtemisDevServicesBuildTimeConfig devServicesBuildTimeConfig, String name,
            boolean isUrlEmpty) {
        if (devServicesBuildTimeConfig != null) {
            return new ArtemisDevServiceCfg(devServicesBuildTimeConfig, name, isUrlEmpty);
        }
        return null;
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
        private final boolean reuse;

        public ArtemisDevServiceCfg(ArtemisDevServicesBuildTimeConfig devServicesConfig,
                String name, boolean isUrlEmpty) {
            this.devServicesEnabled = devServicesConfig.enabled().orElse(isUrlEmpty);
            this.imageName = devServicesConfig.getImageName();
            this.fixedExposedPort = devServicesConfig.getPort();
            this.webUiPort = devServicesConfig.getWebUiPort();
            this.shared = devServicesConfig.isShared();
            this.serviceName = devServicesConfig.getServiceName() + "-" + name;
            this.user = devServicesConfig.getUser();
            this.password = devServicesConfig.getPassword();
            this.extraArgs = devServicesConfig.getExtraArgs();
            this.reuse = devServicesConfig.reuse();
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
        private final boolean useSharedNetwork;
        private final String hostName;
        private final int webUiPort;

        private ArtemisContainer(DockerImageName dockerImageName, int fixedExposedPort, String defaultNetworkId,
                boolean useSharedNetwork, int webUiPort, String user, String password, String extra) {
            super(dockerImageName);
            this.fixedExposedPort = fixedExposedPort;
            this.useSharedNetwork = useSharedNetwork;
            this.hostName = ConfigureUtil.configureNetwork(this, defaultNetworkId, useSharedNetwork, "artemis");
            this.webUiPort = webUiPort;

            withExposedPorts(ARTEMIS_PORT, ARTEMIS_WEB_UI_PORT)
                    .withEnv("AMQ_USER", user)
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
            if (webUiPort > 0) {
                addFixedExposedPort(webUiPort, ARTEMIS_WEB_UI_PORT);
            }
        }

        public int getPort() {
            if (useSharedNetwork) {
                return ARTEMIS_PORT;
            }
            if (fixedExposedPort > 0) {
                return fixedExposedPort;
            }
            return super.getFirstMappedPort();
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
