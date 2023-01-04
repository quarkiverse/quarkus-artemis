package io.quarkus.it.artemis.camel.jms.withdefault.devservices;

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
        return props;
    }

    @Override
    public boolean disableGlobalTestResources() {
        return true;
    }
}
