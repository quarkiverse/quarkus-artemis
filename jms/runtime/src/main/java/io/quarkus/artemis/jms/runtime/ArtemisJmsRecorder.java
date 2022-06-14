package io.quarkus.artemis.jms.runtime;

import java.util.function.Supplier;

import javax.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfig;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ArtemisJmsRecorder {

    final ArtemisRuntimeConfig config;

    public ArtemisJmsRecorder(ArtemisRuntimeConfig config) {
        this.config = config;
    }

    public ArtemisJmsWrapper getDefaultWrapper() {
        return new ArtemisJmsWrapper() {
            @Override
            public ConnectionFactory wrapConnectionFactory(ActiveMQConnectionFactory cf) {
                return cf;
            }
        };
    }

    public Supplier<ConnectionFactory> getConnectionFactorySupplier(ArtemisJmsWrapper wrapper) {
        return new Supplier<ConnectionFactory>() {
            @Override
            public ConnectionFactory get() {
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(config.url,
                        config.username.orElse(null),
                        config.password.orElse(null));
                return wrapper.wrapConnectionFactory(connectionFactory);
            }
        };
    }
}
