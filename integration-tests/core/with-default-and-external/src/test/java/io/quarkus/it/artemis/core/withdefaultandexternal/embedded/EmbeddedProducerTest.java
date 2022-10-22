package io.quarkus.it.artemis.core.withdefaultandexternal.embedded;

import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.it.artemis.core.withdefaultandexternal.BaseArtemisProducerTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ArtemisTestResource.class)
@QuarkusTestResource(NamedOneArtemisTestResource.class)
@QuarkusTestResource(ExternallyDefinedArtemisTestResource.class)
class EmbeddedProducerTest extends BaseArtemisProducerTest {
    @Test
    void testExternallyDefined() throws Exception {
        test(createExternallyDefinedSession(), "test-core-externally-defined", "/artemis/externally-defined");
    }

    private ClientSession createExternallyDefinedSession() throws Exception {
        String url = ConfigProvider.getConfig().getValue("artemis.externally-defined.url", String.class);
        return ActiveMQClient.createServerLocator(url).createSessionFactory().createSession();
    }
}
