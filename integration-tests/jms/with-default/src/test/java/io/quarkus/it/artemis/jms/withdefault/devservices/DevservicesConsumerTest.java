package io.quarkus.it.artemis.jms.withdefault.devservices;

import io.quarkus.it.artemis.jms.withdefault.BaseArtemisConsumerTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevservicesArtemisEnabled.class)
class DevservicesConsumerTest extends BaseArtemisConsumerTest {
}
