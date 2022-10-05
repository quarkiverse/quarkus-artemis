package io.quarkus.it.artemis.core.devservices;

import io.quarkus.it.artemis.core.BaseArtemisProducerTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevServiceArtemisEnabled.class)
class DevservicesProducerTest extends BaseArtemisProducerTest {
}
