package io.quarkus.it.artemis.ra;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(value = ArtemisTestResource.class, restrictToAnnotatedClass = true)
public class JcaResourceTest {

    @BeforeEach
    @Transactional
    void setup() {
        given().when().delete("/jca/gifts").then().statusCode(204);
        given().when().put("/myqueue/reset").then().statusCode(204);
    }

    @Test
    @Transactional
    public void testProducer() throws Exception {
        given().when().get("/jca?name=George").then().statusCode(200).body(is("Hello George"));
        given().when().get("/jca/gifts/count").then().statusCode(200).body(is("1"));
        given().when().get("/myqueue").then().statusCode(200).body(is("1"));
    }

    @Test
    @Transactional
    public void testProducerRollback() throws Exception {
        given().when().get("/jca?name=rollback").then().statusCode(200).body(is("Hello rollback"));
        given().when().get("/jca/gifts/count").then().statusCode(200).body(is("0"));
        given().when().get("/myqueue").then().statusCode(200).body(is("0"));
    }

    @Test
    public void testTransacted() {
        given().when().get("/jca/transacted").then().statusCode(200).body(is("true"));
    }

    @Test
    @Disabled("JMSContext.getTransacted() returns true even if the transaction is not active")
    public void testNotTransacted() {
        given().when().get("/jca/not-transacted").then().statusCode(200).body(is("false"));
    }
}
