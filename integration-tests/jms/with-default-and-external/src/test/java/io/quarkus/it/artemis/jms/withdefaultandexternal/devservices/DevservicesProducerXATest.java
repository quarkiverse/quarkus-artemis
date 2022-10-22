package io.quarkus.it.artemis.jms.withdefaultandexternal.devservices;

import io.quarkus.it.artemis.jms.withdefaultandexternal.BaseArtemisProducerXATest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevservicesArtemisEnabled.class)
class DevservicesProducerXATest extends BaseArtemisProducerXATest {
}
