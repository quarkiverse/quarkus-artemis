package io.quarkus.artemis.core.deployment;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "artemis", phase = ConfigPhase.BUILD_TIME)
public class ArtemisBuildTimeConfig {

    /**
     * Whether or not an health check is published in case the smallrye-health extension is present
     */
    @ConfigItem(name = "health.enabled", defaultValue = "true")
    public boolean healthEnabled;

    /**
     * Support to expose {@link javax.jms.XAConnectionFactory}
     */
    @ConfigItem(name = "xa.enabled", defaultValue = "false")
    public boolean xaEnabled;

    /**
     * Configuration for DevServices. DevServices allows Quarkus to automatically start ActiveMQ Artemis in dev and test mode.
     */
    @ConfigItem
    public ArtemisDevServicesBuildTimeConfig devservices;
}
