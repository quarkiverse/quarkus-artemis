package io.quarkus.it.artemis.camel.jms.withexternal.embedded;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.camel.jms.withexternal.ArtemisHealthCheckHelper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ExternallyDefinedArtemisTestResource.class)
class EmbeddedHealthCheckTest extends ArtemisHealthCheckHelper {
    @Test
    void testHealth() {
        ArtemisHealthCheckHelper.test("/q/health", Collections.emptySet());
    }

    @Test
    void testReady() {
        ArtemisHealthCheckHelper.test("/q/health/ready", Collections.emptySet());
    }
}
