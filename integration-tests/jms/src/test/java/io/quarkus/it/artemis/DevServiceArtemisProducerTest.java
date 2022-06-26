package io.quarkus.it.artemis;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevServiceArtemisEnabled.class)
public class DevServiceArtemisProducerTest extends BaseArtemisProducerTest {
    @Test
    public void test() throws Exception {
        super.test();
    }
}
