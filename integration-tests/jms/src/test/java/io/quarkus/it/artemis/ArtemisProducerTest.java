package io.quarkus.it.artemis;

import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ArtemisTestResource.class)
public class ArtemisProducerTest extends BaseArtemisProducerTest {
    @Test
    public void test() throws Exception {
        super.test();
    }
}
