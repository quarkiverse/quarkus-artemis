package io.quarkus.artemis.core.runtime.health;

import java.util.Set;
import java.util.function.Supplier;

import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ArtemisHealthSupportRecorder {
    public Supplier<ArtemisHealthSupport> getArtemisHealthSupportBuilder(Set<String> names) {
        return () -> new ArtemisHealthSupport(names);
    }
}
