package io.quarkus.it.artemis.core.withoutdefault.embedded;

import io.quarkus.it.artemis.core.withoutdefault.BaseArtemisConsumerTest;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(NamedOneArtemisTestResource.class)
class EmbeddedConsumerTest extends BaseArtemisConsumerTest {
}
