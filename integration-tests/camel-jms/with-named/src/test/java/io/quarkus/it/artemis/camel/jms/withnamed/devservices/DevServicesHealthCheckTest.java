package io.quarkus.it.artemis.camel.jms.withnamed.devservices;

import io.quarkus.it.artemis.camel.jms.withnamed.BaseArtemisHealthCheckTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevservicesArtemisEnabled.class)
class DevServicesHealthCheckTest extends BaseArtemisHealthCheckTest {
}
