package io.quarkus.it.artemis.core.withlocatorcustomization.devservices;

import io.quarkus.it.artemis.core.withlocatorcustomization.BaseArtemisCustomizationTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevServiceArtemisEnabled.class)
class DevservicesCustomizationTest extends BaseArtemisCustomizationTest {
}
