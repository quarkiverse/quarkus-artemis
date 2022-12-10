package io.quarkus.it.artemis.camel.jms.withexternal.embedded;

import io.quarkus.it.artemis.camel.jms.withexternal.BaseSendAndReceiveTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ExternallyDefinedArtemisTestResource.class)
class EmbeddedSendAndReceiveTest extends BaseSendAndReceiveTest {
}
