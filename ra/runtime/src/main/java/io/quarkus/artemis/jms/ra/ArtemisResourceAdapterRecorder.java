package io.quarkus.artemis.jms.ra;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.activemq.artemis.api.core.client.ServerLocator;

import io.quarkiverse.ironjacamar.runtime.IronJacamarRuntimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisCoreRecorder;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ArtemisResourceAdapterRecorder extends ArtemisCoreRecorder {
    public Supplier<ServerLocator> getServerLocatorSupplier(String name, IronJacamarRuntimeConfig ironJacamarRuntimeConfig) {
        Pattern pattern = Pattern.compile("host=(.*);port=(.*);protocols=(.*)");
        Map<String, String> props = ironJacamarRuntimeConfig.resourceAdapters().get(name).ra().config();
        String connectionParameters = props.get("connection-parameters");
        String url = null;
        if (connectionParameters != null) {
            Matcher matcher = pattern.matcher(connectionParameters);
            if (matcher.find()) {
                url = "tcp://" + matcher.group(1) + ":" + matcher.group(2);
            }
        }
        ServerLocator serverLocator = Objects.requireNonNull(getServerLocator(url));
        return () -> serverLocator;
    }
}
