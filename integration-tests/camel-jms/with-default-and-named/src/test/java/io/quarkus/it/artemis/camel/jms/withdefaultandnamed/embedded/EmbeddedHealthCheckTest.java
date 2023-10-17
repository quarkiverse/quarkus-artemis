package io.quarkus.it.artemis.camel.jms.withdefaultandnamed.embedded;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.camel.jms.withdefaultandnamed.BaseArtemisHealthCheck;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ArtemisTestResource.class)
@QuarkusTestResource(NamedArtemisTestResource.class)
class EmbeddedHealthCheckTest extends BaseArtemisHealthCheck {
}
