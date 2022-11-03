package io.quarkus.artemis.core.runtime.health;

import java.util.*;
import java.util.function.Supplier;

import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ArtemisHealthSupportRecorder {
    public Supplier<ArtemisHealthSupport> getArtemisSupportBuilder(
            Set<String> names,
            Set<String> excludedNames) {
        return new Supplier<>() {
            @Override
            public ArtemisHealthSupport get() {
                return new ArtemisHealthSupport(names, excludedNames);
            }
        };
    }
}
