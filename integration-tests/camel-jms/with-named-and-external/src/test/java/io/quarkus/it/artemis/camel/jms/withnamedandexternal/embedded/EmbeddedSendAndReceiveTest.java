package io.quarkus.it.artemis.camel.jms.withnamedandexternal.embedded;

import io.quarkus.it.artemis.camel.jms.common.BaseSendAndReceiveTest;
import io.quarkus.it.artemis.camel.jms.withnamedandexternal.ArtemisEndpoint;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(NamedArtemisTestResource.class)
@WithTestResource(ExternallyDefinedArtemisTestResource.class)
@TestHTTPEndpoint(ArtemisEndpoint.class)
class EmbeddedSendAndReceiveTest implements BaseSendAndReceiveTest {
}
