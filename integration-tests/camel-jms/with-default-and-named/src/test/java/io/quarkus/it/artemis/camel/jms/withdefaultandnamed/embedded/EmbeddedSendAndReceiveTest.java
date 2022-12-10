package io.quarkus.it.artemis.camel.jms.withdefaultandnamed.embedded;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.camel.jms.withdefaultandnamed.BaseSendAndReceiveTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ArtemisTestResource.class)
@QuarkusTestResource(NamedArtemisTestResource.class)
class EmbeddedSendAndReceiveTest extends BaseSendAndReceiveTest {
}
