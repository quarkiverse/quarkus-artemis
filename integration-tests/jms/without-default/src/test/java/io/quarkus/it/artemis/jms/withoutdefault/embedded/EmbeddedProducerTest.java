package io.quarkus.it.artemis.jms.withoutdefault.embedded;

import io.quarkus.it.artemis.jms.withoutdefault.BaseArtemisProducerTest;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(NamedOneArtemisTestResource.class)
class EmbeddedProducerTest extends BaseArtemisProducerTest {
}
