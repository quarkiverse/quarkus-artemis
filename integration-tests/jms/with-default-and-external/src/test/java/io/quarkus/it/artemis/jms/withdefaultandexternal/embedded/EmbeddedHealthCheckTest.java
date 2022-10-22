package io.quarkus.it.artemis.jms.withdefaultandexternal.embedded;

import java.util.Set;

import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.jms.withdefaultandexternal.ArtemisHealthCheckHelper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ArtemisTestResource.class)
@QuarkusTestResource(NamedOneArtemisTestResource.class)
@QuarkusTestResource(ExternallyDefinedArtemisTestResource.class)
class EmbeddedHealthCheckTest extends ArtemisHealthCheckHelper {
    @Test
    void testHealth() {
        ArtemisHealthCheckHelper.test("/q/health", Set.of("<default>", "named-1", "externally-defined"));
    }

    @Test
    void testReady() {
        ArtemisHealthCheckHelper.test("/q/health/ready", Set.of("<default>", "named-1", "externally-defined"));
    }
}
