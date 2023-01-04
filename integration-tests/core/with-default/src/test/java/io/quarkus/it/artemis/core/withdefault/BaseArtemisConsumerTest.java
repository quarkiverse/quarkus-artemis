package io.quarkus.it.artemis.core.withdefault;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.core.common.ArtemisCoreHelper;

public abstract class BaseArtemisConsumerTest extends ArtemisCoreHelper {
    @Test
    void testDefault() throws Exception {
        sendAndVerify(createDefaultSession(), "test-core-default", "/artemis");
    }

    @Test
    void testNamedOne() throws Exception {
        sendAndVerify(createNamedOneSession(), "test-core-named-1", "/artemis/named-1");
    }
}
