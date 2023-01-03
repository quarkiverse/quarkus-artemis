package io.quarkus.artemis.jms.deployment;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "artemis", phase = ConfigPhase.BUILD_TIME)
public class ArtemisJmsBuildTimeConfig {
    /**
     * Whether camel context enhancement should be enabled.
     * <p>
     * If enabled, all {@link javax.jms.ConnectionFactory}s annotated with
     * {@link io.smallrye.common.annotation.Identifier} are registered as named beans in the camel
     * context.
     */
    @ConfigItem(name = "camel-quarkus-enhance-enabled")
    public boolean camelQuarkusEnhanceEnable = false;

    public boolean isCamelQuarkusEnhanceEnable() {
        return camelQuarkusEnhanceEnable;
    }
}
