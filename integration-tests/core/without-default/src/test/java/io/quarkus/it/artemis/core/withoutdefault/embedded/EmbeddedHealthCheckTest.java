package io.quarkus.it.artemis.core.withoutdefault.embedded;

import io.quarkus.it.artemis.core.withoutdefault.BaseArtemisHealthCheckTest;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(NamedOneArtemisTestResource.class)
class EmbeddedHealthCheckTest extends BaseArtemisHealthCheckTest {
}
