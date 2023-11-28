package io.quarkus.artemis.jms.ra.deployment;

import java.util.Collection;
import java.util.stream.Stream;

import org.apache.activemq.artemis.api.core.client.loadbalance.ConnectionLoadBalancingPolicy;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.ra.ActiveMQResourceAdapter;
import org.apache.activemq.artemis.spi.core.remoting.ClientProtocolManagerFactory;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import io.netty.channel.epoll.EpollSocketChannel;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;

public class ArtemisResourceAdapterProcessor {
    private static final String FEATURE = "artemis-jms-ra";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void indexDependencies(BuildProducer<IndexDependencyBuildItem> indexedDependency) {
        indexedDependency.produce(new IndexDependencyBuildItem("org.apache.activemq", "artemis-core-client"));
        indexedDependency.produce(new IndexDependencyBuildItem("org.apache.activemq", "artemis-*-protocol"));
    }

    @BuildStep
    void reflectiveClasses(CombinedIndexBuildItem combinedIndex, BuildProducer<ReflectiveClassBuildItem> reflectiveClasses) {
        reflectiveClasses.produce(ReflectiveClassBuildItem.builder(
                // org.apache.activemq
                NettyConnectorFactory.class,
                ActiveMQResourceAdapter.class,
                // io.netty
                EpollSocketChannel.class).methods().build());

        final IndexView index = combinedIndex.getIndex();

        reflectiveClasses.produce(
                ReflectiveClassBuildItem.builder(
                        Stream.of(ClientProtocolManagerFactory.class, ConnectionLoadBalancingPolicy.class)
                                .map(DotName::createSimple)
                                .map(index::getAllKnownImplementors)
                                .flatMap(Collection::stream)
                                .map(ClassInfo::name)
                                .map(DotName::toString)
                                .toArray(String[]::new))
                        .methods()
                        .build());
    }

    @BuildStep
    void runtimeInitializedClasses(BuildProducer<RuntimeInitializedClassBuildItem> runtimeInitializedClasses) {
        runtimeInitializedClasses.produce(
                new RuntimeInitializedClassBuildItem("org.apache.activemq.artemis.utils.RandomUtil"));
    }
}
