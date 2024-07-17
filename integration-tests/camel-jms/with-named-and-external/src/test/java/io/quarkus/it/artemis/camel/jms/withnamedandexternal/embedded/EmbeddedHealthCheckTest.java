package io.quarkus.it.artemis.camel.jms.withnamedandexternal.embedded;

import java.util.Set;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.common.ArtemisHealthCheckHelper;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(NamedArtemisTestResource.class)
@WithTestResource(ExternallyDefinedArtemisTestResource.class)
class EmbeddedHealthCheckTest {
    @Test
    void testHealth() {
        ArtemisHealthCheckHelper.testJms("/q/health", Set.of("named", "externally-defined"));
    }

    @Test
    void testReady() {
        ArtemisHealthCheckHelper.testJms("/q/health/ready", Set.of("named", "externally-defined"));
    }
}
