package io.quarkus.it.artemis.core.withexternal.embedded;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.core.common.ArtemisCoreHelper;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(ExternallyDefinedArtemisTestResource.class)
class EmbeddedConsumerTest extends ArtemisCoreHelper {
    @Test
    void testExternallyDefined() throws Exception {
        sendAndVerify(
                createExternallyDefinedSession("artemis.externally-defined.url"),
                "test-core-externally-defined",
                "/artemis/externally-defined");
    }
}
