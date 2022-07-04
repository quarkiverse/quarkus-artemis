package io.quarkus.it.artemis;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class DevServiceArtemisEnabled implements QuarkusTestProfile {
    public DevServiceArtemisEnabled() {
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        Map<String, String> props = new HashMap<>();
        props.put("quarkus.artemis.devservices.enabled", "true");
        props.put("quarkus.artemis.devservices.extra-args", "--no-autotune --queues test-core");

        return props;
    }

    @Override
    public boolean disableGlobalTestResources() {
        return true;
    }
}
