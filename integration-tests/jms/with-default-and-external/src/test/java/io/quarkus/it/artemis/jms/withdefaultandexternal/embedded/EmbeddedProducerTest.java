package io.quarkus.it.artemis.jms.withdefaultandexternal.embedded;

import javax.jms.JMSContext;

import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.jms.withdefaultandexternal.BaseArtemisProducerTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ArtemisTestResource.class)
@QuarkusTestResource(NamedOneArtemisTestResource.class)
@QuarkusTestResource(ExternallyDefinedArtemisTestResource.class)
class EmbeddedProducerTest extends BaseArtemisProducerTest {
    @Test
    void testExternallyDefined() throws Exception {
        test(createExternallyDefinedContext(), "test-jms-externally-defined", "/artemis/externally-defined");
    }

    private JMSContext createExternallyDefinedContext() {
        String url = ConfigProvider.getConfig().getValue("artemis.externally-defined.url", String.class);
        return new ActiveMQJMSConnectionFactory(url).createContext(JMSContext.AUTO_ACKNOWLEDGE);
    }
}
