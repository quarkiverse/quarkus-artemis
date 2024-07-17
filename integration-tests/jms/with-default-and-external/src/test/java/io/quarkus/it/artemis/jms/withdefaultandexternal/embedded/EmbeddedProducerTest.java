package io.quarkus.it.artemis.jms.withdefaultandexternal.embedded;

import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.jms.withdefaultandexternal.BaseArtemisProducerTest;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(ArtemisTestResource.class)
@WithTestResource(NamedOneArtemisTestResource.class)
@WithTestResource(ExternallyDefinedArtemisTestResource.class)
class EmbeddedProducerTest extends BaseArtemisProducerTest {
    @Test
    void testExternallyDefined() throws Exception {
        receiveAndVerify(
                "/artemis/externally-defined",
                createExternallyDefinedContext("artemis.externally-defined.url"),
                "test-jms-externally-defined");
    }
}
