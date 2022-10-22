package io.quarkus.it.artemis.jms.withdefaultandexternal.embedded;

import io.quarkus.artemis.test.ArtemisTestResource;

public class NamedOneArtemisTestResource extends ArtemisTestResource {
    public NamedOneArtemisTestResource() {
        super("named-1");
    }
}
