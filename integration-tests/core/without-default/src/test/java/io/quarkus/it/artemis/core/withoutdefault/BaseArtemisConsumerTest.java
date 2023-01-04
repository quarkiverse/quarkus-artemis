package io.quarkus.it.artemis.core.withoutdefault;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.core.common.ArtemisCoreHelper;

public abstract class BaseArtemisConsumerTest extends ArtemisCoreHelper {
    @Test
    void testNamedOne() throws Exception {
        sendAndVerify(createNamedOneSession(), "test-core-named-1", "/artemis/named-1");
    }
}
