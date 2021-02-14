package com.firecontroller1847.classicui;

import com.firecontroller1847.classicui.gui.cmm.ClassicMainMenuScreen;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {

    // Config Spec
    public static final ForgeConfigSpec SPEC = Config.build();

    // General
    public static ConfigValue<Boolean> enableDebug;
    public static EnumValue<SupportedVersion> selectedVersion;

    // Disabled Features
    public static ConfigValue<List<? extends String>> fscmm;

    private static ForgeConfigSpec build() {
        // Create the builder
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // @formatter:off
        /////////////
        // General //
        /////////////
        builder.push("General");

        enableDebug = builder
                .comment("When enabled, an extra level of debugging output will be provided to the logs.\n" +
                         "Press ESC on the Main Menu to refresh most GUIs when enabled.")
                .define("Debug", false);

        selectedVersion = builder
                .comment(
                    "The main game version to use for general UI building.\n" +
                    "Following sections allow you to disable features. Those override the default settings."
                ).defineEnum("GameVersion", SupportedVersion.R1_16);

        builder.pop();

        ///////////////////////
        // Disabled Features //
        ///////////////////////
        builder.push("DisabledFeatures");

        fscmm = builder
                .comment(
                    "Add a feature to the list to disable it. Requires restart for ClassicMainMenu.\n" +
                    "Valid Values: " + String.join(", ", Arrays.stream(ClassicMainMenuScreen.Feature.values()).map(Enum::toString).toArray(String[]::new))
                ).defineList("ClassicMainMenu", ArrayList::new, feature -> {
                    try {
                        ClassicMainMenuScreen.Feature.valueOf((String) feature);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });

        builder.pop();
        // @formatter:on

        // Build the spec
        return builder.build();
    }

}
