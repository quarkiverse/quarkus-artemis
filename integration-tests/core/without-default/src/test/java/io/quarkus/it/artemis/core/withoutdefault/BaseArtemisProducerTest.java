package io.quarkus.it.artemis.core.withoutdefault;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.core.common.ArtemisCoreHelper;

public abstract class BaseArtemisProducerTest extends ArtemisCoreHelper {
    @Test
    void testNamedOne() throws Exception {
        receiveAndVerify("/artemis/named-1", createNamedOneSession(), "test-core-named-1");
    }
}
