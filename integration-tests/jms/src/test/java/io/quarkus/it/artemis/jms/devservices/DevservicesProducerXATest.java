package io.quarkus.it.artemis.jms.devservices;

import io.quarkus.it.artemis.jms.BaseArtemisProducerXATest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevservicesArtemisEnabled.class)
class DevservicesProducerXATest extends BaseArtemisProducerXATest {
}
