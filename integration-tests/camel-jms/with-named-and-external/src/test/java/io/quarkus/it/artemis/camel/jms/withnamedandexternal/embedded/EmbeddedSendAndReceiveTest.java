package io.quarkus.it.artemis.camel.jms.withnamedandexternal.embedded;

import io.quarkus.it.artemis.camel.jms.withnamedandexternal.BaseSendAndReceiveTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(NamedArtemisTestResource.class)
@QuarkusTestResource(ExternallyDefinedArtemisTestResource.class)
class EmbeddedSendAndReceiveTest extends BaseSendAndReceiveTest {
}
