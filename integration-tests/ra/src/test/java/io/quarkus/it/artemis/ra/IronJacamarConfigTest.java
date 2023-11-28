package io.quarkus.it.artemis.ra;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkiverse.ironjacamar.runtime.IronJacamarRuntimeConfig;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class IronJacamarConfigTest {

    @Inject
    IronJacamarRuntimeConfig runtimeConfig;

    @Test
    void shouldReadConfig() {
        assertThat(runtimeConfig.resourceAdapters().get("<default>").ra()).satisfies(
                ra -> {
                    assertThat(ra.config()).hasEntrySatisfying("connection-parameters", cp -> {
                        assertThat(cp).matches("host=localhost;port=[0-9]+;protocols=AMQP");
                    });
                });
        assertThat(runtimeConfig.activationSpecs().map().get("myqueue").config()).hasEntrySatisfying("destination", d -> {
            assertThat(d).endsWith("MyQueue");
        });

    }
}
