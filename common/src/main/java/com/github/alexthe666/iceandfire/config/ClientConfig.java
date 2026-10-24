package com.github.alexthe666.iceandfire.config;

import dev.vladeezyy.iceandfire.platform.ConfigSpec;

public class ClientConfig {

    public final ConfigSpec.BooleanValue customMainMenu;
    public final ConfigSpec.BooleanValue useVanillaFont;
    public final ConfigSpec.BooleanValue dragonAuto3rdPerson;

    public ClientConfig(final ConfigSpec.Builder builder) {
        builder.push("general");
        this.customMainMenu = buildBoolean(builder, "Custom main menu", "all", true, "Whether to display the dragon on the main menu or not");
        this.dragonAuto3rdPerson = buildBoolean(builder, "Auto 3rd person when riding dragon", "all", true, "True if riding dragons should make the player take a 3rd person view automatically.");
        this.useVanillaFont = buildBoolean(builder, "Use Vanilla Font", "all", false, "Whether to use the vanilla font in the bestiary or not");    }

    private static ConfigSpec.BooleanValue buildBoolean(ConfigSpec.Builder builder, String name, String catagory, boolean defaultValue, String comment){
        return builder.comment(comment).translation(name).define(name, defaultValue);
    }

    private static ConfigSpec.IntValue buildInt(ConfigSpec.Builder builder, String name, String catagory, int defaultValue, int min, int max, String comment){
        return builder.comment(comment).translation(name).defineInRange(name, defaultValue, min, max);
    }

    private static ConfigSpec.DoubleValue buildDouble(ConfigSpec.Builder builder, String name, String catagory, double defaultValue, double min, double max, String comment){
        return builder.comment(comment).translation(name).defineInRange(name, defaultValue, min, max);
    }
}
