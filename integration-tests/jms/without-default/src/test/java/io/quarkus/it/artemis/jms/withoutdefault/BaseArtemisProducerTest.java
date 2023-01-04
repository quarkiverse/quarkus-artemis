package io.quarkus.it.artemis.jms.withoutdefault;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.jms.common.ArtemisJmsHelper;

abstract public class BaseArtemisProducerTest extends ArtemisJmsHelper {
    @Test
    void testNamedOne() throws Exception {
        receiveAndVerify("/artemis/named-1", createNamedOneContext(), "test-jms-named-1");
    }

    @Test
    void testXANamedOne() throws Exception {
        receiveAndVerify("/artemis/named-1/xa", createNamedOneContext(), "test-jms-named-1");
    }

    @Test
    void testRollbackNamedOne() {
        testRollback("/artemis/named-1/xa", createNamedOneContext(), "test-jms-named-1");
    }
}
