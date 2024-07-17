package io.quarkus.it.artemis.camel.jms.withdefaultandnamed.embedded;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.camel.jms.withdefaultandnamed.BaseArtemisHealthCheck;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(ArtemisTestResource.class)
@WithTestResource(NamedArtemisTestResource.class)
class EmbeddedHealthCheckTest extends BaseArtemisHealthCheck {
}
