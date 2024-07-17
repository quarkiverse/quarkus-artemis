package io.quarkus.it.artemis.core.withdefault.changeurl.embedded;

import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.core.common.ArtemisCoreHelper;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(ArtemisTestResource.class)
@WithTestResource(NamedOneArtemisTestResource.class)
class EmbeddedProducerTest extends ArtemisCoreHelper {
    @Test
    void testDefault() throws Exception {
        receiveAndVerify("/artemis", createDefaultSession(), "test-core-default");
    }

    @Test
    void testNamedOne() throws Exception {
        receiveAndVerify("/artemis/named-1", createNamedOneSession(), "test-core-named-1");
    }
}
