package io.quarkus.it.artemis.jms.withdefaultandexternal.embedded;

import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.jms.withdefaultandexternal.BaseArtemisConsumerTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ArtemisTestResource.class)
@QuarkusTestResource(NamedOneArtemisTestResource.class)
@QuarkusTestResource(ExternallyDefinedArtemisTestResource.class)
class EmbeddedConsumerTest extends BaseArtemisConsumerTest {
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
