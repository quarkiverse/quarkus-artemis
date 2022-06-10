package io.quarkus.artemis.jms.deployment;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.ConnectionFactory;
import javax.jms.XAConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQXAConnectionFactory;

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
            BuildProducer<SyntheticBeanBuildItem> syntheticBeanProducer) {
        SyntheticBeanBuildItem connectionFactory = SyntheticBeanBuildItem
                .configure(ActiveMQJMSConnectionFactory.class)
                .addType(ConnectionFactory.class)
                .supplier(recorder.getConnectionFactorySupplier())
                .scope(ApplicationScoped.class)
                .defaultBean()
                .unremovable()
                .setRuntimeInit()
                .done();
        syntheticBeanProducer.produce(connectionFactory);

        if (config.xa) {
            SyntheticBeanBuildItem xaConnectionFactory = SyntheticBeanBuildItem
                    .configure(ActiveMQXAConnectionFactory.class)
                    .addType(XAConnectionFactory.class)
                    .supplier(recorder.getXAConnectionFactorySupplier())
                    .scope(ApplicationScoped.class)
                    .defaultBean()
                    .unremovable()
                    .setRuntimeInit()
                    .done();
            syntheticBeanProducer.produce(xaConnectionFactory);
        }

        return new ArtemisJmsConfiguredBuildItem();
    }
}
