package io.quarkus.it.artemis.core.withdefaultdynamicport.devservices;

import io.quarkus.it.artemis.core.withdefaultdynamicport.BaseArtemisConsumerTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevServiceArtemisEnabled.class)
class DevservicesConsumerTest extends BaseArtemisConsumerTest {
}