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
            BuildProducer<SyntheticBeanBuildItem> syntheticBeanProducer) {

        SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator;
        if (config.xaEnabled) {
            configurator = SyntheticBeanBuildItem.configure(ActiveMQConnectionFactory.class);
            /**
             * Since {@link ActiveMQConnectionFactory} implements both {@link ConnectionFactory} and
             * {@link XAConnectionFactory},
             * even with "quarkus.artemis.xa.enabled=true" we still need to export ConnectionFactory which is used by
             * {@link io.quarkus.artemis.jms.runtime.health.ConnectionFactoryHealthCheck} for health checking.
             */
            configurator.addType(XAConnectionFactory.class);
            configurator.addType(ConnectionFactory.class);
        } else {
            configurator = SyntheticBeanBuildItem.configure(ConnectionFactory.class);
        }

        configurator.supplier(recorder.getConnectionFactorySupplier(
                wrapper.orElseGet(() -> new ArtemisJmsWrapperBuildItem(recorder.getDefaultWrapper())).getWrapper()))
                .scope(ApplicationScoped.class)
                .defaultBean()
                .unremovable()
                .setRuntimeInit();

        syntheticBeanProducer.produce(configurator.done());

        return new ArtemisJmsConfiguredBuildItem();
    }
}
