package io.quarkus.it.artemis.core.withexternal.embedded;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.core.withexternal.BaseArtemisProducerTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ArtemisTestResource.class)
@QuarkusTestResource(NamedOneArtemisTestResource.class)
@QuarkusTestResource(ExternallyDefinedArtemisTestResource.class)
class EmbeddedProducerTest extends BaseArtemisProducerTest {
}
