package io.quarkus.it.artemis.core.withoutdefault.embedded;

import io.quarkus.artemis.test.ArtemisTestResource;

public class NamedOneArtemisTestResource extends ArtemisTestResource {
    public NamedOneArtemisTestResource() {
        super("named-1");
    }
}
