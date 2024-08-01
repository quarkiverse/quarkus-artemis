package io.quarkus.it.artemis.ra;

import java.util.Set;

import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.common.ArtemisHealthCheckHelper;
import io.quarkus.it.artemis.ra.profile.DisableDataBaseService;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DisableDataBaseService.class)
@QuarkusTestResource(ArtemisTestResource.class)
class HealthCheckTest {
    @Test
    void testHealth() {
        ArtemisHealthCheckHelper.testJmsRA("/q/health", Set.of("<default>"));
    }

    @Test
    void testReady() {
        ArtemisHealthCheckHelper.testJmsRA("/q/health/ready", Set.of("<default>"));
    }
}
