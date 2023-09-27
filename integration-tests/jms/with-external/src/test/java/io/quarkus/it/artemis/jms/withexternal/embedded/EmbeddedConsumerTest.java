package io.quarkus.it.artemis.jms.withexternal.embedded;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.jms.common.ArtemisJmsHelper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ExternallyDefinedArtemisTestResource.class)
class EmbeddedConsumerTest extends ArtemisJmsHelper {
    @Test
    void testExternallyDefined() {
        sendAndVerify(
                createExternallyDefinedContext("artemis.externally-defined.url"),
                "test-jms-externally-defined",
                "/artemis/externally-defined");
    }

    @Test
    void testExternallyDefinedXACommit() {
        sendAndVerifyXACommit(createExternallyDefinedContext("artemis.externally-defined.url"), "test-jms-externally-defined",
                "/artemis/externally-defined/xa",
                "/artemis/externally-defined/");
    }

    @Test
    void testExternallyDefinedXARollback() {
        sendAndVerifyXARollback(createExternallyDefinedContext("artemis.externally-defined.url"),
                "test-jms-externally-defined",
                "/artemis/externally-defined/xa-rollback",
                "/artemis/externally-defined/");
    }
}
