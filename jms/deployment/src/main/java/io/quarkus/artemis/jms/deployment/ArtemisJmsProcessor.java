package io.quarkus.artemis.jms.deployment;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.ConnectionFactory;
import javax.jms.XAConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.artemis.core.deployment.ArtemisBuildTimeConfig;
import io.quarkus.artemis.core.deployment.ArtemisJmsBuildItem;
import io.quarkus.artemis.jms.runtime.ArtemisJmsRecorder;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;

public class ArtemisJmsProcessor {

    private static final String FEATURE = "artemis-jms";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void load(BuildProducer<ArtemisJmsBuildItem> artemisJms) {
        artemisJms.produce(new ArtemisJmsBuildItem());
    }

    @BuildStep
    HealthBuildItem health(ArtemisBuildTimeConfig buildConfig) {
        return new HealthBuildItem(
                "io.quarkus.artemis.jms.runtime.health.ConnectionFactoryHealthCheck",
                buildConfig.healthEnabled);
    }

    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep
    ArtemisJmsConfiguredBuildItem configure(ArtemisJmsRecorder recorder, ArtemisBuildTimeConfig config,
            Optional<ArtemisJmsWrapperBuildItem> wrapper,
            Capabilities capabilities,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeanProducer) {

        SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator;
        if (config.xaEnabled) {
            configurator = SyntheticBeanBuildItem.configure(ActiveMQConnectionFactory.class);
            configurator.addType(XAConnectionFactory.class);
        } else {
            configurator = SyntheticBeanBuildItem.configure(ConnectionFactory.class);
        }

        configurator.addType(ConnectionFactory.class)
                .supplier(recorder.getConnectionFactorySupplier(
                        wrapper.orElseGet(() -> new ArtemisJmsWrapperBuildItem(recorder.getDefaultWrapper()))
                                .getWrapper(),
                        capabilities.isPresent(Capability.TRANSACTIONS)))
                .scope(ApplicationScoped.class)
                .defaultBean()
                .unremovable()
                .setRuntimeInit();

        syntheticBeanProducer.produce(configurator.done());

        return new ArtemisJmsConfiguredBuildItem();
    }
}
