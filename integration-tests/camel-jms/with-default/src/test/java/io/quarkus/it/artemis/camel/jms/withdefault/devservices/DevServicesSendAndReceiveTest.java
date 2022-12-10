package io.quarkus.it.artemis.camel.jms.withdefault.devservices;

import io.quarkus.it.artemis.camel.jms.withdefault.BaseSendAndReceiveTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevservicesArtemisEnabled.class)
class DevServicesSendAndReceiveTest extends BaseSendAndReceiveTest {
}
