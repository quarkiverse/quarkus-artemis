package io.quarkus.it.artemis.core.withlocatorcustomization.devservices;

import io.quarkus.it.artemis.core.withlocatorcustomization.BaseArtemisHealthCheckTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevServiceArtemisEnabled.class)
class DevservicesHealthCheckTest extends BaseArtemisHealthCheckTest {
}
