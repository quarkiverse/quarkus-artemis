package io.quarkus.it.artemis.core.withdefaultandexternal.embedded;

import java.util.Set;

import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.common.ArtemisHealthCheckHelper;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(ArtemisTestResource.class)
@WithTestResource(NamedOneArtemisTestResource.class)
@WithTestResource(ExternallyDefinedArtemisTestResource.class)
class EmbeddedHealthCheckTest {
    @Test
    void testHealth() {
        ArtemisHealthCheckHelper.testCore("/q/health", Set.of("<default>", "named-1", "externally-defined"));
    }

    @Test
    void testReady() {
        ArtemisHealthCheckHelper.testCore("/q/health/ready", Set.of("<default>", "named-1", "externally-defined"));
    }
}
