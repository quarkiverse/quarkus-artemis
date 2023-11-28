package io.quarkus.artemis.jms.ra;

import java.util.Map;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.XAConnectionFactory;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.endpoint.MessageEndpoint;

import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.ra.ActiveMQRAManagedConnectionFactory;
import org.apache.activemq.artemis.ra.ActiveMQResourceAdapter;
import org.apache.activemq.artemis.ra.inflow.ActiveMQActivationSpec;
import org.apache.activemq.artemis.utils.VersionLoader;

import io.quarkiverse.ironjacamar.ResourceAdapterFactory;
import io.quarkiverse.ironjacamar.ResourceAdapterKind;
import io.quarkiverse.ironjacamar.ResourceAdapterTypes;
import io.quarkiverse.ironjacamar.runtime.endpoint.MessageEndpointWrapper;

/**
 * Integration with IronJacamar for Artemis
 */
@ResourceAdapterKind(value = "artemis")
@ResourceAdapterTypes(connectionFactoryTypes = { ConnectionFactory.class, XAConnectionFactory.class })
public class ArtemisResourceAdapterFactory implements ResourceAdapterFactory {

    @Override
    public String getDescription() {
        return String.format("%s %s", ActiveMQResourceAdapter.PRODUCT_NAME, VersionLoader.getVersion().getFullVersion());
    }

    @Override
    public ActiveMQResourceAdapter createResourceAdapter(String id, Map<String, String> config) {
        ActiveMQResourceAdapter adapter = new ActiveMQResourceAdapter();
        adapter.setConnectorClassName(NettyConnectorFactory.class.getName());
        adapter.setConnectionParameters(config.get("connection-parameters"));
        adapter.setProtocolManagerFactoryStr(config.get("protocol-manager-factory"));
        adapter.setUseJNDI(false);
        adapter.setUserName(config.get("user"));
        adapter.setPassword(config.get("password"));
        adapter.setIgnoreJTA(false);
        return adapter;
    }

    @Override
    public ManagedConnectionFactory createManagedConnectionFactory(String id, ResourceAdapter adapter)
            throws ResourceException {
        ActiveMQRAManagedConnectionFactory factory = new ActiveMQRAManagedConnectionFactory();
        factory.setResourceAdapter(adapter);
        factory.setAllowLocalTransactions(true);
        return factory;
    }

    @Override
    public ActivationSpec createActivationSpec(String id, ResourceAdapter adapter, Class<?> type, Map<String, String> config)
            throws ResourceException {
        ActiveMQActivationSpec activationSpec = new ActiveMQActivationSpec();
        activationSpec.setResourceAdapter(adapter);
        activationSpec.setDestinationType(config.get("destination-type"));
        activationSpec.setDestination(config.get("destination"));
        activationSpec.setMaxSession(Integer.valueOf(config.getOrDefault("max-session", "5")));
        activationSpec.setRebalanceConnections(Boolean.valueOf(config.getOrDefault("rebalance-connections", "true")));
        activationSpec.setUseJNDI(false);
        return activationSpec;
    }

    @Override
    public MessageEndpoint wrap(MessageEndpoint endpoint, Object resourceEndpoint) {
        return new JMSMessageEndpoint(endpoint, (MessageListener) resourceEndpoint);
    }

    private static class JMSMessageEndpoint extends MessageEndpointWrapper implements MessageListener {

        private final MessageListener listener;

        public JMSMessageEndpoint(MessageEndpoint messageEndpoint, MessageListener listener) {
            super(messageEndpoint);
            this.listener = listener;
        }

        @Override
        public void onMessage(Message message) {
            listener.onMessage(message);
        }
    }
}
