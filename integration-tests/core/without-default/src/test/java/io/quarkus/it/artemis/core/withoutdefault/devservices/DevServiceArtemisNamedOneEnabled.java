package io.quarkus.it.artemis.core.withoutdefault.devservices;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class DevServiceArtemisNamedOneEnabled implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        Map<String, String> props = new HashMap<>();
        props.put("quarkus.artemis.\"named-1\".devservices.enabled", "true");
        props.put("quarkus.artemis.\"named-1\".devservices.extra-args", "--no-autotune --queues test-core-named-1");
        return props;
    }

    @Override
    public boolean disableGlobalTestResources() {
        return true;
    }
}
