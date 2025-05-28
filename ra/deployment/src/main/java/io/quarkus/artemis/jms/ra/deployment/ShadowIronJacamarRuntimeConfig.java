/*
 * Copyright (c) 2025 by Bank Lombard Odier & Co Ltd, Geneva, Switzerland. This software is subject
 * to copyright protection under the laws of Switzerland and other countries. ALL RIGHTS RESERVED.
 *
 */
package io.quarkus.artemis.jms.ra.deployment;

import java.util.Map;
import java.util.Optional;

import io.quarkiverse.ironjacamar.Defaults;
import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefaults;
import io.smallrye.config.WithParentName;
import io.smallrye.config.WithUnnamedKey;

@ConfigMapping(prefix = "quarkus.ironjacamar")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface ShadowIronJacamarRuntimeConfig {

    /**
     * Resource Adapters
     */
    @ConfigDocMapKey("resource-adapter-name")
    @WithParentName
    @WithDefaults
    @WithUnnamedKey(Defaults.DEFAULT_RESOURCE_ADAPTER_NAME)
    Map<String, ResourceAdapterOuterNamedConfig> resourceAdapters();

    @ConfigGroup
    interface ResourceAdapterOuterNamedConfig {

        /**
         * The Resource adapter configuration.
         */
        ResourceAdapterConfig ra();
    }

    interface ResourceAdapterConfig {
        /**
         * The configuration for this resource adapter
         */
        Config config();

    }

    interface Config {
        /**
         * The connection string url for this resource adapter
         */
        Optional<String> connectionParameters();
    }
}
