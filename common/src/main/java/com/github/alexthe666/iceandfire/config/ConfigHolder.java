package com.github.alexthe666.iceandfire.config;

import dev.vladeezyy.iceandfire.platform.ConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class ConfigHolder {

    public static final ConfigSpec CLIENT_SPEC;
    public static final ConfigSpec SERVER_SPEC;
    public static final ClientConfig CLIENT;
    public static final ServerConfig SERVER;

    static {
        {
            final Pair<ClientConfig, ConfigSpec> specPair = new ConfigSpec.Builder().configure(ClientConfig::new);
            CLIENT = specPair.getLeft();
            CLIENT_SPEC = specPair.getRight();
        }
        {
            final Pair<ServerConfig, ConfigSpec> specPair = new ConfigSpec.Builder().configure(ServerConfig::new);
            SERVER = specPair.getLeft();
            SERVER_SPEC = specPair.getRight();
        }
    }
}