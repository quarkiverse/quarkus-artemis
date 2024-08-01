package io.quarkus.artemis.jms.ra.deployment;

import java.util.Map.Entry;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;

import org.apache.activemq.artemis.api.core.client.ServerLocator;

import io.quarkiverse.ironjacamar.runtime.IronJacamarBuildtimeConfig;
import io.quarkiverse.ironjacamar.runtime.IronJacamarBuildtimeConfig.ResourceAdapterOuterNamedConfig;
import io.quarkiverse.ironjacamar.runtime.IronJacamarRuntimeConfig;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.artemis.core.deployment.ArtemisJmsRABuildItem;
import io.quarkus.artemis.core.deployment.health.ExtraArtemisHealthCheckBuildItem;
import io.quarkus.artemis.core.runtime.ArtemisUtil;
import io.quarkus.artemis.jms.ra.ArtemisResourceAdapterRecorder;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.smallrye.common.annotation.Identifier;

public class ArtemisResourceAdapterProcessor {
    private static final String FEATURE = "artemis-jms-ra";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void load(BuildProducer<ArtemisJmsRABuildItem> ra) {
        ra.produce(new ArtemisJmsRABuildItem());
    }

    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep
    void setupServerLocatorForHealthCheck(ArtemisResourceAdapterRecorder recorder,
            IronJacamarBuildtimeConfig ironJacamarBuildtimeConfig,
            IronJacamarRuntimeConfig ironJacamarRuntimeConfig,
            BuildProducer<ExtraArtemisHealthCheckBuildItem> healthCheck,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeanProducer) {
        for (Entry<String, ResourceAdapterOuterNamedConfig> config : ironJacamarBuildtimeConfig.resourceAdapters().entrySet()) {
            if (config.getValue().ra().kind().isPresent()) {
                String kind = config.getValue().ra().kind().get();
                if (kind.equals("artemis")) {
                    String name = config.getKey();
                    SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator = SyntheticBeanBuildItem
                            .configure(ServerLocator.class)
                            .supplier(recorder.getServerLocatorSupplier(name, ironJacamarRuntimeConfig))
                            .scope(ApplicationScoped.class)
                            .setRuntimeInit();
                    if (ArtemisUtil.isDefault(name)) {
                        configurator
                                .unremovable()
                                .addQualifier().annotation(Default.class).done()
                                .name(name);
                    } else {
                        configurator
                                .addQualifier().annotation(Identifier.class).addValue("value", name).done();
                    }
                    syntheticBeanProducer.produce(configurator.done());
                    healthCheck.produce(new ExtraArtemisHealthCheckBuildItem(name));
                }
            }
        }
    }
}
