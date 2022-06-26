package io.quarkus.it.artemis;

import java.util.Collections;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class DevServiceArtemisEnabled implements QuarkusTestProfile {
    public DevServiceArtemisEnabled() {
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        return Collections.singletonMap("quarkus.artemis.devservices.enabled", "true");
    }

    @Override
    public boolean disableGlobalTestResources() {
        return true;
    }
}
