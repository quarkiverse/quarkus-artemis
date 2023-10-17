package io.quarkus.it.artemis.camel.jms.withdefaultandnamed.devservices;

import io.quarkus.it.artemis.camel.jms.withdefaultandnamed.BaseArtemisHealthCheck;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevservicesArtemisEnabled.class)
class DevServicesHealthCheckTest extends BaseArtemisHealthCheck {
}
