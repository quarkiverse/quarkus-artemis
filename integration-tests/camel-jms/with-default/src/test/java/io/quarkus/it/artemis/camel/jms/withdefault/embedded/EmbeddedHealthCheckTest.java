package io.quarkus.it.artemis.camel.jms.withdefault.embedded;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.camel.jms.withdefault.BaseArtemisHealthCheckTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ArtemisTestResource.class)
class EmbeddedHealthCheckTest extends BaseArtemisHealthCheckTest {
}
