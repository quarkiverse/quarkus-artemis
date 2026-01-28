package io.quarkus.it.artemis.core.withfactorycustomization.devservices;

import io.quarkus.it.artemis.core.withfactorycustomization.BaseArtemisCustomizationTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevServiceArtemisEnabled.class)
class DevservicesCustomizationTest extends BaseArtemisCustomizationTest {
}
