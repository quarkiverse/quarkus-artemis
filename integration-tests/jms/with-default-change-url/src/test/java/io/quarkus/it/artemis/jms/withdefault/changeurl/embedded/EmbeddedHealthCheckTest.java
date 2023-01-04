package io.quarkus.it.artemis.jms.withdefault.changeurl.embedded;

import java.util.Set;

import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.common.ArtemisHealthCheckHelper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ArtemisTestResource.class)
@QuarkusTestResource(NamedOneArtemisTestResource.class)
class EmbeddedHealthCheckTest {
    @Test
    void testHealth() {
        ArtemisHealthCheckHelper.testJms("/q/health", Set.of("<default>", "named-1"));
    }

    @Test
    void testReady() {
        ArtemisHealthCheckHelper.testJms("/q/health/ready", Set.of("<default>", "named-1"));
    }
}
