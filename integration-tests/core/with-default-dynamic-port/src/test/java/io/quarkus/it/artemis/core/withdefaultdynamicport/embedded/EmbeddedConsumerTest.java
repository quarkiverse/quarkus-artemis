package io.quarkus.it.artemis.core.withdefaultdynamicport.embedded;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.core.withdefaultdynamicport.BaseArtemisConsumerTest;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(ArtemisTestResource.class)
@WithTestResource(value = ArtemisTestResource.class, initArgs = { @ResourceArg(name = "configurationName", value = "named-1") })
class EmbeddedConsumerTest extends BaseArtemisConsumerTest {
}
