package io.quarkus.it.artemis.camel.jms.withdefaultandnamed.embedded;

import java.util.Set;

import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.camel.jms.withdefaultandnamed.ArtemisHealthCheckHelper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ArtemisTestResource.class)
@QuarkusTestResource(NamedArtemisTestResource.class)
class EmbeddedHealthCheckTest extends ArtemisHealthCheckHelper {
    @Test
    void testHealth() {
        ArtemisHealthCheckHelper.test("/q/health", Set.of("<default>", "named"));
    }

    @Test
    void testReady() {
        ArtemisHealthCheckHelper.test("/q/health/ready", Set.of("<default>", "named"));
    }
}
