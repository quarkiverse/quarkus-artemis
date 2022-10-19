package io.quarkus.it.artemis.core.withoutdefault.devservices;

import io.quarkus.it.artemis.core.withoutdefault.BaseArtemisProducerTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevServiceArtemisNamedOneEnabled.class)
class DevservicesProducerTest extends BaseArtemisProducerTest {
}
