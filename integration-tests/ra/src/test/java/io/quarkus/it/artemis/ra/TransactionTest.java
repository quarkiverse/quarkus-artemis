package io.quarkus.it.artemis.ra;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;

import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(value = ArtemisTestResource.class, restrictToAnnotatedClass = true)
public class TransactionTest {

    @Inject
    ConnectionFactory factory;

    @Inject
    SalesEndpoint endpoint;

    @Test
    public void retryMessagesOnRollback() throws Exception {
        given().when().formParam("name", "George").post("/jca/sales").then().statusCode(204);
        try (JMSContext context = factory.createContext()) {
            JMSConsumer consumer = context.createConsumer(context.createQueue("inventory"));
            // Ensure that no message was sent to the inventory queue
            assertThat(consumer.receive(1000L)).isNull();
            // Check if the message was redelivered 3 times
            assertThat(endpoint.getCount()).isEqualTo(3);
        }

    }
}
