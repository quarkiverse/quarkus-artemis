package io.quarkus.artemis.core.runtime.graal;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collection;
import java.util.List;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.TargetClass;

public class JGroupSubstitutions {
}

@TargetClass(org.jgroups.util.Util.class)
final class Target_Util {
    @Alias
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.Reset)
    static List<NetworkInterface> CACHED_INTERFACES;

    @Alias
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.Reset)
    static Collection<InetAddress> CACHED_ADDRESSES;
}
