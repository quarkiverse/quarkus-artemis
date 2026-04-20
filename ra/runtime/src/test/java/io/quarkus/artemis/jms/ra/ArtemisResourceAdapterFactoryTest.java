package io.quarkus.artemis.jms.ra;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

class ArtemisResourceAdapterFactoryTest {

    @Test
    void createResourceAdapterWithNullConnectionParameters() {
        ArtemisResourceAdapterFactory factory = new ArtemisResourceAdapterFactory();
        assertDoesNotThrow(() -> factory.createResourceAdapter("test", Map.of()));
    }

    @Test
    void createResourceAdapterWithConnectionParameters() {
        ArtemisResourceAdapterFactory factory = new ArtemisResourceAdapterFactory();
        var adapter = factory.createResourceAdapter("test",
                Map.of("connection-parameters", "host=localhost;port=61616"));
        assertNotNull(adapter);
    }

    @Test
    void createResourceAdapterWithMultipleHosts() {
        ArtemisResourceAdapterFactory factory = new ArtemisResourceAdapterFactory();
        var adapter = factory.createResourceAdapter("test",
                Map.of("connection-parameters", "host=host1;port=61616,host=host2;port=61617"));
        assertNotNull(adapter);
    }
}
