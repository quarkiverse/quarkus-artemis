package io.quarkus.it.artemis.core.withoutdefault.devservices;

import io.quarkus.it.artemis.core.withoutdefault.BaseArtemisConsumerTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevServiceArtemisNamedOneEnabled.class)
class DevservicesConsumerTest extends BaseArtemisConsumerTest {
}