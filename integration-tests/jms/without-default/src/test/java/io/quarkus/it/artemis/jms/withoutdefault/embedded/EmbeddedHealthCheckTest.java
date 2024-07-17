package io.quarkus.it.artemis.jms.withoutdefault.embedded;

import io.quarkus.it.artemis.jms.withoutdefault.BaseArtemisHealthCheckTest;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(NamedOneArtemisTestResource.class)
class EmbeddedHealthCheckTest extends BaseArtemisHealthCheckTest {
}
