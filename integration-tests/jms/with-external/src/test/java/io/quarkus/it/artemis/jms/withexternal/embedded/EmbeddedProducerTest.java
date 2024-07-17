package io.quarkus.it.artemis.jms.withexternal.embedded;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.jms.common.ArtemisJmsHelper;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(ExternallyDefinedArtemisTestResource.class)
class EmbeddedProducerTest extends ArtemisJmsHelper {
    @Test
    void testExternallyDefined() throws Exception {
        receiveAndVerify(
                "/artemis/externally-defined",
                createExternallyDefinedContext("artemis.externally-defined.url"),
                "test-jms-externally-defined");
    }

    @Test
    void testXAExternallyDefined() throws Exception {
        receiveAndVerify(
                "/artemis/externally-defined/xa",
                createExternallyDefinedContext("artemis.externally-defined.url"),
                "test-jms-externally-defined");
    }

    @Test
    void testRollbackExternallyDefined() {
        testRollback(
                "/artemis/externally-defined/xa",
                createExternallyDefinedContext("artemis.externally-defined.url"),
                "test-jms-externally-defined");
    }
}
