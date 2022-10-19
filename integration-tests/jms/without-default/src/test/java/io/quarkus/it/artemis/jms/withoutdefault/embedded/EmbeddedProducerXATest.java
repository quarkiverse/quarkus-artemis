package io.quarkus.it.artemis.jms.withoutdefault.embedded;

import io.quarkus.it.artemis.jms.withoutdefault.BaseArtemisProducerXATest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(NamedOneArtemisTestResource.class)
class EmbeddedProducerXATest extends BaseArtemisProducerXATest {
}
