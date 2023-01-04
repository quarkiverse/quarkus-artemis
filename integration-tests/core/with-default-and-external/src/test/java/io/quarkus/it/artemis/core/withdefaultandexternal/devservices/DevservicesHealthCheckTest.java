package io.quarkus.it.artemis.core.withdefaultandexternal.devservices;

import java.util.Set;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.common.ArtemisHealthCheckHelper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevServiceArtemisEnabled.class)
class DevservicesHealthCheckTest {
    @Test
    void testHealth() {
        ArtemisHealthCheckHelper.testCore("/q/health", Set.of("<default>", "named-1"));
    }

    @Test
    void testReady() {
        ArtemisHealthCheckHelper.testCore("/q/health/ready", Set.of("<default>", "named-1"));
    }
}
