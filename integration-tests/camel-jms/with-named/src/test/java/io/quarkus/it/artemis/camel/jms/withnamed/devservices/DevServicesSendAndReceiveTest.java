package io.quarkus.it.artemis.camel.jms.withnamed.devservices;

import io.quarkus.it.artemis.camel.jms.common.BaseSendAndReceiveTest;
import io.quarkus.it.artemis.camel.jms.withnamed.ArtemisEndpoint;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevservicesArtemisEnabled.class)
@TestHTTPEndpoint(ArtemisEndpoint.class)
class DevServicesSendAndReceiveTest implements BaseSendAndReceiveTest {
}
