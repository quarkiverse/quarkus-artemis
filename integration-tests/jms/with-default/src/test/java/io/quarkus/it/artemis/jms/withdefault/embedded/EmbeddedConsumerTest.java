package io.quarkus.it.artemis.jms.withdefault.embedded;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.jms.withdefault.BaseArtemisConsumerTest;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(ArtemisTestResource.class)
@WithTestResource(NamedOneArtemisTestResource.class)
class EmbeddedConsumerTest extends BaseArtemisConsumerTest {
}
