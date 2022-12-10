package io.quarkus.it.artemis.camel.jms.withdefault.embedded;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.camel.jms.withdefault.BaseSendAndReceiveTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ArtemisTestResource.class)
class EmbeddedSendAndReceiveTest extends BaseSendAndReceiveTest {
}
