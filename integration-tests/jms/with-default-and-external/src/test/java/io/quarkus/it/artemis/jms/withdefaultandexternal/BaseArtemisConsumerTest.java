package io.quarkus.it.artemis.jms.withdefaultandexternal;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.jms.common.ArtemisJmsHelper;

public abstract class BaseArtemisConsumerTest extends ArtemisJmsHelper {
    @Test
    void testDefault() {
        sendAndVerify(createDefaultContext(), "test-jms-default", "/artemis");
    }

    @Test
    void testNamedOne() {
        sendAndVerify(createNamedOneContext(), "test-jms-named-1", "/artemis/named-1");
    }

    @Test
    void testDefaultXACommit() {
        sendAndVerifyXACommit(createDefaultContext(), "test-jms-default", "/artemis/xa", "/artemis");
    }

    @Test
    void testNamedOneXACommit() {
        sendAndVerifyXACommit(createNamedOneContext(), "test-jms-named-1", "/artemis/named-1/xa", "/artemis/named-1/");
    }

    @Test
    void testDefaultXARollback() {
        sendAndVerifyXARollback(createDefaultContext(), "test-jms-default", "/artemis/xa-rollback", "/artemis");
    }

    @Test
    void testNamedOneXARollback() {
        sendAndVerifyXARollback(createNamedOneContext(), "test-jms-named-1", "/artemis/named-1/xa-rollback",
                "/artemis/named-1/");
    }
}
