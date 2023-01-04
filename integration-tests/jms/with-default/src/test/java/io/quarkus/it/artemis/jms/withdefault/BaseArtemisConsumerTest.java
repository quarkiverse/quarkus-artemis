package io.quarkus.it.artemis.jms.withdefault;

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
}
