package io.quarkus.it.artemis.jms.withdefaultandexternal.devservices;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class DevservicesArtemisEnabled implements QuarkusTestProfile {
    public DevservicesArtemisEnabled() {
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        Map<String, String> props = new HashMap<>();
        props.put("quarkus.artemis.devservices.enabled", "true");
        props.put("quarkus.artemis.\"named-1\".devservices.enabled", "true");
        props.put("quarkus.artemis.health.external.enabled", "false");
        return props;
    }

    @Override
    public boolean disableGlobalTestResources() {
        return true;
    }
}
