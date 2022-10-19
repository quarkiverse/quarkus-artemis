package io.quarkus.artemis.core.runtime.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

// Epoll and Kqueue are not supported on SVM
@SuppressWarnings("unused")
@Substitute
@TargetClass(org.apache.activemq.artemis.core.remoting.impl.netty.CheckDependencies.class)
final class CheckDependenciesSubstitutions {
    private CheckDependenciesSubstitutions() {
    }

    @Substitute
    public static boolean isEpollAvailable() {
        return false;
    }

    @Substitute
    public static boolean isKQueueAvailable() {
        return false;
    }
}
