package io.quarkus.it.artemis.core.withdefaultandexternal.embedded;

import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.core.withdefaultandexternal.BaseArtemisConsumerTest;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(ArtemisTestResource.class)
@WithTestResource(NamedOneArtemisTestResource.class)
@WithTestResource(ExternallyDefinedArtemisTestResource.class)
class EmbeddedConsumerTest extends BaseArtemisConsumerTest {
    @Test
    void testExternallyDefined() throws Exception {
        sendAndVerify(
                createExternallyDefinedSession("artemis.externally-defined.url"),
                "test-core-externally-defined",
                "/artemis/externally-defined");
    }
}
