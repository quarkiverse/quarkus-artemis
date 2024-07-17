package io.quarkus.it.artemis.core.withdefault.changeurl.embedded;

import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.core.common.ArtemisCoreHelper;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(ArtemisTestResource.class)
@WithTestResource(NamedOneArtemisTestResource.class)
class EmbeddedConsumerTest extends ArtemisCoreHelper {
    @Test
    void testDefault() throws Exception {
        sendAndVerify(createDefaultSession(), "test-core-default", "/artemis");
    }

    @Test
    void testNamedOne() throws Exception {
        sendAndVerify(createNamedOneSession(), "test-core-named-1", "/artemis/named-1");
    }
}
