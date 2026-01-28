package io.quarkus.it.artemis.core.withlocatorcustomization.devservices;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class DevServiceArtemisEnabled implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        Map<String, String> props = new HashMap<>();
        props.put("quarkus.artemis.devservices.enabled", "true");
        props.put("quarkus.artemis.devservices.extra-args", "--no-autotune --queues test-core-default");
        props.put("quarkus.artemis.\"named-1\".devservices.enabled", "true");
        props.put("quarkus.artemis.\"named-1\".devservices.extra-args", "--no-autotune --queues test-core-named-1");
        return props;
    }

    @Override
    public boolean disableGlobalTestResources() {
        return true;
    }
}
