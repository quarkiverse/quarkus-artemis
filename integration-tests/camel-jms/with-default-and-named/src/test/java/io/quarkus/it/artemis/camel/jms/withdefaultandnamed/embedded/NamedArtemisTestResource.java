package io.quarkus.it.artemis.camel.jms.withdefaultandnamed.embedded;

import io.quarkus.artemis.test.ArtemisTestResource;

public class NamedArtemisTestResource extends ArtemisTestResource {
    public NamedArtemisTestResource() {
        super("named");
    }
}