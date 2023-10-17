package io.quarkus.it.artemis.camel.jms.withdefaultandnamed;

import java.util.Set;

import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.camel.jms.withdefaultandnamed.embedded.NamedArtemisTestResource;
import io.quarkus.it.artemis.common.ArtemisHealthCheckHelper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ArtemisTestResource.class)
@QuarkusTestResource(NamedArtemisTestResource.class)
public abstract class BaseArtemisHealthCheck {
    @Test
    void testHealth() {
        ArtemisHealthCheckHelper.testJms("/q/health", Set.of("<default>"));
    }

    @Test
    void testReady() {
        ArtemisHealthCheckHelper.testJms("/q/health/ready", Set.of("<default>"));
    }
}
