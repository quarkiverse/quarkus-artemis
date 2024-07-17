package io.quarkus.it.artemis.ra;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
@WithTestResource(ArtemisTestResource.class)
class TransactionTest {
    @Inject
    @SuppressWarnings("CdiInjectionPointsInspection")
    ConnectionFactory factory;

    @Inject
    SalesEndpoint endpoint;

    @Test
    void retryMessagesOnRollback() {
        // @formatter:off
        RestAssured
                .given().formParam("name", "George")
                .when().post("/jca/sales")
                .then().statusCode(Response.Status.NO_CONTENT.getStatusCode());
        // @formatter:on

        try (JMSContext context = factory.createContext()) {
            JMSConsumer consumer = context.createConsumer(context.createQueue("inventory"));
            // Ensure that no message was sent to the inventory queue
            assertThat(consumer.receive(1000L)).isNull();
            // Check if the message was redelivered 3 times
            assertThat(endpoint.getCount()).isEqualTo(3);
        }
    }
}
