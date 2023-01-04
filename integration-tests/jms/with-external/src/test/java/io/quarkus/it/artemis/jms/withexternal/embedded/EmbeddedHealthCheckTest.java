package io.quarkus.it.artemis.jms.withexternal.embedded;

import java.util.Set;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.common.ArtemisHealthCheckHelper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ExternallyDefinedArtemisTestResource.class)
class EmbeddedHealthCheckTest {
    @Test
    void testHealth() {
        ArtemisHealthCheckHelper.testJms("/q/health", Set.of("externally-defined"));
    }

    @Test
    void testReady() {
        ArtemisHealthCheckHelper.testJms("/q/health/ready", Set.of("externally-defined"));
    }
}
