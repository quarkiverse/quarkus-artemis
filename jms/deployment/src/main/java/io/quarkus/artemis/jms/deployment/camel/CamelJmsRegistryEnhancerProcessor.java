package io.quarkus.artemis.jms.deployment.camel;

import org.apache.camel.quarkus.core.CamelCapabilities;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.artemis.core.runtime.ArtemisBuildTimeConfigs;
import io.quarkus.artemis.jms.runtime.camel.CamelContextEnhancer;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.annotations.BuildStep;

public class CamelJmsRegistryEnhancerProcessor {
    @SuppressWarnings("unused")
    @BuildStep
    AdditionalBeanBuildItem addCamelContextEnhancer(Capabilities capabilities,
            ArtemisBuildTimeConfigs buildTimeConfigs) {
        if (buildTimeConfigs.isCamelQuarkusEnhanceEnable()
                && capabilities.isPresent(CamelCapabilities.CORE)) {
            return AdditionalBeanBuildItem.unremovableOf(CamelContextEnhancer.class);
        }
        return null;
    }
}
