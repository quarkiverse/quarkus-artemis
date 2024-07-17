package io.quarkus.it.artemis.camel.jms.withnamed.embedded;

import io.quarkus.it.artemis.camel.jms.withnamed.BaseArtemisHealthCheckTest;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(NamedArtemisTestResource.class)
class EmbeddedHealthCheckTest extends BaseArtemisHealthCheckTest {
}
