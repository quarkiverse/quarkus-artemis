package io.quarkus.it.artemis.camel.jms.withnamed.devservices;

import java.util.Set;

import org.junit.jupiter.api.Test;

import io.quarkus.it.artemis.camel.jms.withnamed.ArtemisHealthCheckHelper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevservicesArtemisEnabled.class)
class DevServicesHealthCheckTest extends ArtemisHealthCheckHelper {
    @Test
    void testHealth() {
        ArtemisHealthCheckHelper.test("/q/health", Set.of("named"));
    }

    @Test
    void testReady() {
        ArtemisHealthCheckHelper.test("/q/health/ready", Set.of("named"));
    }
}
