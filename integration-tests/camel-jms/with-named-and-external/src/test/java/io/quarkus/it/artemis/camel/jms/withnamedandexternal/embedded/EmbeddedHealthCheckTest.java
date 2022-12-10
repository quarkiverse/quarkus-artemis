package io.quarkus.it.artemis.camel.jms.withnamedandexternal.embedded;

import java.util.Set;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.camel.jms.withnamedandexternal.ArtemisHealthCheckHelper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(NamedArtemisTestResource.class)
@QuarkusTestResource(ExternallyDefinedArtemisTestResource.class)
class EmbeddedHealthCheckTest extends ArtemisHealthCheckHelper {
    @Test
    void testHealth() {
        ArtemisHealthCheckHelper.test("/q/health", Set.of("named", "externally-defined"));
    }

    @Test
    void testReady() {
        ArtemisHealthCheckHelper.test("/q/health/ready", Set.of("named", "externally-defined"));
    }
}
