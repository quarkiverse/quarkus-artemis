package io.quarkus.it.artemis.jms.opentelemetry.embedded;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.jms.opentelemetry.BaseOpenTelemetryTest;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(ArtemisTestResource.class)
class EmbeddedOpenTelemetryTest extends BaseOpenTelemetryTest {
}
