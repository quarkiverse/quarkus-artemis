package io.quarkus.it.artemis.camel.jms.withdefaultandnamed;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.jms;

import org.apache.camel.builder.RouteConfigurationBuilder;

@SuppressWarnings("unused")
public class TransferRoute extends RouteConfigurationBuilder {

    @Override
    public void configuration() {
        from(jms("queue:in").connectionFactory("<default>"))
                .to(jms("queue:out").connectionFactory("named"));
    }
}
