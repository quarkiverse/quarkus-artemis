package io.quarkus.it.artemis.ra.profile;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class DisableDataBaseService implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("quarkus.datasource.devservices.enabled", "false",
                "quarkus.hibernate-orm.enabled", "false",
                "quarkus.datasource.health.enabled", "false");
    }
}
