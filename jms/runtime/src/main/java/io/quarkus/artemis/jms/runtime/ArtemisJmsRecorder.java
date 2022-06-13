package io.quarkus.artemis.jms.runtime;

import java.util.function.Supplier;

import javax.jms.ConnectionFactory;
import javax.transaction.TransactionManager;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import io.quarkus.arc.Arc;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfig;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ArtemisJmsRecorder {

    final ArtemisRuntimeConfig config;

    public ArtemisJmsRecorder(ArtemisRuntimeConfig config) {
        this.config = config;
    }

    public ArtemisJmsWrapper getDefaultWrapper() {
        return (cf, tm) -> cf;
    }

    public Supplier<ConnectionFactory> getConnectionFactorySupplier(ArtemisJmsWrapper wrapper, boolean transaction) {
        return new Supplier<ConnectionFactory>() {
            @Override
            public ConnectionFactory get() {
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(config.url,
                        config.username.orElse(null),
                        config.password.orElse(null));
                return wrapper.wrapConnectionFactory(connectionFactory,
                        transaction ? Arc.container().instance(TransactionManager.class).get() : null);
            }
        };
    }
}
