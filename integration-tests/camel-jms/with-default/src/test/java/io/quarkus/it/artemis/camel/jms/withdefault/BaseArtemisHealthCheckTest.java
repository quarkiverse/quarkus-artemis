package io.quarkus.it.artemis.camel.jms.withdefault;

import java.util.Set;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.common.ArtemisHealthCheckHelper;

public abstract class BaseArtemisHealthCheckTest {
    @Test
    void testHealth() {
        ArtemisHealthCheckHelper.testJms("/q/health", Set.of("<default>"));
    }

    @Test
    void testReady() {
        ArtemisHealthCheckHelper.testJms("/q/health/ready", Set.of("<default>"));
    }
}
