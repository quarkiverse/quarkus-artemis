package io.quarkus.it.artemis.ra;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;

import org.junit.jupiter.api.Test;

import io.quarkiverse.ironjacamar.runtime.IronJacamarContainer;
import io.quarkus.arc.Arc;
import io.quarkus.it.artemis.ra.profile.DisableAllServices;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DisableAllServices.class)
class InjectionTest {
    @Inject
    @SuppressWarnings("CdiInjectionPointsInspection")
    ConnectionFactory connectionFactory;

    @Test
    void testProducer() {
        assertThat(Arc.container().listAll(IronJacamarContainer.class)).hasSize(1);
    }

    @Test
    void shouldInjectConnectionFactory() {
        assertThat(Arc.container().listAll(ConnectionFactory.class)).hasSize(1);
        assertThat(connectionFactory).isNotNull();
    }

}
