package io.quarkus.it.artemis.core.withlocatorcustomization.embedded;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.core.withlocatorcustomization.BaseArtemisCustomizationTest;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(ArtemisTestResource.class)
@WithTestResource(NamedOneArtemisTestResource.class)
class EmbeddedCustomizationTest extends BaseArtemisCustomizationTest {
}
