package io.quarkus.it.artemis.jms.empty;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.common.ArtemisHealthCheckHelper;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class HealthCheckTest {
    @Test
    void testHealth() {
        ArtemisHealthCheckHelper.testJms("/q/health", Collections.emptySet());
    }

    @Test
    void testReady() {
        ArtemisHealthCheckHelper.testJms("/q/health/ready", Collections.emptySet());
    }
}
