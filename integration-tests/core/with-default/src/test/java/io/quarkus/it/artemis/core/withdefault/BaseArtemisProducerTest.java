package io.quarkus.it.artemis.core.withdefault;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.core.common.ArtemisCoreHelper;

public abstract class BaseArtemisProducerTest extends ArtemisCoreHelper {
    @Test
    void testDefault() throws Exception {
        receiveAndVerify("/artemis", createDefaultSession(), "test-core-default");
    }

    @Test
    void testNamedOne() throws Exception {
        receiveAndVerify("/artemis/named-1", createNamedOneSession(), "test-core-named-1");
    }
}
