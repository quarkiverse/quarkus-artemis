package io.quarkus.it.artemis.camel.jms.withexternal.embedded;

import io.quarkus.it.artemis.camel.jms.common.BaseSendAndReceiveTest;
import io.quarkus.it.artemis.camel.jms.withexternal.ArtemisEndpoint;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(ExternallyDefinedArtemisTestResource.class)
@TestHTTPEndpoint(ArtemisEndpoint.class)
class EmbeddedSendAndReceiveTest implements BaseSendAndReceiveTest {
}
