package io.quarkus.artemis.jms.runtime.camel;

import java.util.Collections;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.jms.ConnectionFactory;

import org.apache.camel.impl.event.CamelContextInitializingEvent;

import io.quarkus.artemis.core.runtime.ArtemisUtil;

@ApplicationScoped
public class CamelContextEnhancer {
    void startUp(@Observes CamelContextInitializingEvent event) {
        Map<String, ConnectionFactory> connectionFactoryNamesFromArc = ArtemisUtil
                .extractIdentifiers(ConnectionFactory.class, Collections.emptySet());
        for (var entry : connectionFactoryNamesFromArc.entrySet()) {
            event.getContext().getRegistry().bind(entry.getKey(), entry.getValue());
        }
    }
}
