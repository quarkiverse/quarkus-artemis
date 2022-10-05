package io.quarkus.it.artemis.jms.devservices;

import io.quarkus.it.artemis.jms.BaseArtemisProducerTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevservicesArtemisEnabled.class)
class DevservicesProducerTest extends BaseArtemisProducerTest {
}
