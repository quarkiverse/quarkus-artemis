package io.quarkus.it.artemis.core.withdefaultandexternal.embedded;

import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.core.withdefaultandexternal.BaseArtemisProducerTest;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(ArtemisTestResource.class)
@WithTestResource(NamedOneArtemisTestResource.class)
@WithTestResource(ExternallyDefinedArtemisTestResource.class)
class EmbeddedProducerTest extends BaseArtemisProducerTest {
    @Test
    void testExternallyDefined() throws Exception {
        receiveAndVerify("/artemis/externally-defined", createExternallyDefinedSession("artemis.externally-defined.url"),
                "test-core-externally-defined");
    }
}
