package io.quarkus.it.artemis.camel.jms.withnamed.embedded;

import io.quarkus.it.artemis.camel.jms.common.BaseSendAndReceiveTest;
import io.quarkus.it.artemis.camel.jms.withnamed.ArtemisEndpoint;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(NamedArtemisTestResource.class)
@TestHTTPEndpoint(ArtemisEndpoint.class)
class EmbeddedSendAndReceiveTest implements BaseSendAndReceiveTest {
}
