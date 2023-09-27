package io.quarkus.it.artemis.jms.withoutdefault;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.jms.common.ArtemisJmsHelper;

public abstract class BaseArtemisConsumerTest extends ArtemisJmsHelper {
    @Test
    void testNamedOne() {
        sendAndVerify(createNamedOneContext(), "test-jms-named-1", "/artemis/named-1");
    }

    @Test
    void testNamedOneXACommit() {
        sendAndVerifyXACommit(createNamedOneContext(), "test-jms-named-1", "/artemis/named-1/xa", "/artemis/named-1/");
    }

    @Test
    void testNamedOneXARollback() {
        sendAndVerifyXARollback(createNamedOneContext(), "test-jms-named-1", "/artemis/named-1/xa-rollback",
                "/artemis/named-1/");
    }
}
