package io.quarkus.it.artemis.core.withexternal.embedded;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.core.common.ArtemisCoreHelper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ExternallyDefinedArtemisTestResource.class)
class EmbeddedProducerTest extends ArtemisCoreHelper {
    @Test
    void testExternallyDefined() throws Exception {
        receiveAndVerify("/artemis/externally-defined", createExternallyDefinedSession("artemis.externally-defined.url"),
                "test-core-externally-defined");
    }
}
