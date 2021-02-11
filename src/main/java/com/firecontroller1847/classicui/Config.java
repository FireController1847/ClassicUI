package com.firecontroller1847.classicui;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class Config {

    // Config Spec
    public static final ForgeConfigSpec SPEC = Config.build();

    // General
    public static ConfigValue<Boolean> enableDebug;

    private static ForgeConfigSpec build() {
        // Create the builder
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // @formatter:off
        /////////////
        // General //
        /////////////
        builder.push("General");

        enableDebug = builder
                .comment("When enabled, an extra level of debugging output will be provided to the logs.")
                .define("Debug", false);

        builder.pop();
        // @formatter:on

        // Build the spec
        return builder.build();
    }

}
