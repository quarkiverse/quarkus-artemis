package io.quarkus.it.artemis.camel.jms.withdefault.embedded;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.camel.jms.common.BaseSendAndReceiveTest;
import io.quarkus.it.artemis.camel.jms.withdefault.ArtemisEndpoint;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(ArtemisTestResource.class)
@TestHTTPEndpoint(ArtemisEndpoint.class)
class EmbeddedSendAndReceiveTest implements BaseSendAndReceiveTest {
}
