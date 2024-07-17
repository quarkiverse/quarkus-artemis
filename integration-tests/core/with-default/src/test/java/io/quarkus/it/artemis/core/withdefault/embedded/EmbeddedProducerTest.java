package io.quarkus.it.artemis.core.withdefault.embedded;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.core.withdefault.BaseArtemisProducerTest;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(ArtemisTestResource.class)
@WithTestResource(NamedOneArtemisTestResource.class)
class EmbeddedProducerTest extends BaseArtemisProducerTest {
}
