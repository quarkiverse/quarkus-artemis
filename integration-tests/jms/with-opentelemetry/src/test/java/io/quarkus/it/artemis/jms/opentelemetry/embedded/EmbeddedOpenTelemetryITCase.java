package io.quarkus.it.artemis.jms.opentelemetry.embedded;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.jms.opentelemetry.BaseOpenTelemetryTest;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
@WithTestResource(ArtemisTestResource.class)
class EmbeddedOpenTelemetryITCase extends BaseOpenTelemetryTest {
}
