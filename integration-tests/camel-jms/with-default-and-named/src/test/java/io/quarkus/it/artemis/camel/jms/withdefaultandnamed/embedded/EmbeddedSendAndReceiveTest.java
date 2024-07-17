package io.quarkus.it.artemis.camel.jms.withdefaultandnamed.embedded;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.camel.jms.common.BaseSendAndReceiveTest;
import io.quarkus.it.artemis.camel.jms.withdefaultandnamed.ArtemisEndpoint;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(ArtemisTestResource.class)
@WithTestResource(NamedArtemisTestResource.class)
@TestHTTPEndpoint(ArtemisEndpoint.class)
class EmbeddedSendAndReceiveTest implements BaseSendAndReceiveTest {
}
