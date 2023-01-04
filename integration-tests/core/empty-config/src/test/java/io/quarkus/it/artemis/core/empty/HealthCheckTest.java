package io.quarkus.it.artemis.core.empty;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.common.ArtemisHealthCheckHelper;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class HealthCheckTest {
    @Test
    void testHealth() {
        ArtemisHealthCheckHelper.testCore("/q/health", Collections.emptySet());
    }

    @Test
    void testReady() {
        ArtemisHealthCheckHelper.testCore("/q/health/ready", Collections.emptySet());
    }
}
