package com.firecontroller1847.classicui;

import com.firecontroller1847.classicui.gui.cmm.ClassicMainMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

// TODO:
//  - Add the ability to load in textures and resources from older game versions on the launcher if installed
//  - Add support for more game versions
//  - Add support for more screens other than just the menu screen
@Mod(ClassicUI.MOD_ID)
@EventBusSubscriber(modid = ClassicUI.MOD_ID, bus = Bus.MOD)
public class ClassicUI {

    private static ClassicUI instance;
    public static final String MOD_ID = "classicui";
    public static final String MOD_NAME = "ClassicUI";
    public static final String MOD_VERSION = "1.0.0";
    private static final Logger logger = LogManager.getLogger(ClassicUI.MOD_ID);

    // Initializer
    public ClassicUI() {
        instance = this;
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC, "classicui.toml");
    }

    // Register Events
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (Minecraft.getInstance().isDemo()) {
            logger.error("This mod is incompatible with the demo version of Minecraft. Please log in!");
            return;
        }

        // Register Events
        MinecraftForge.EVENT_BUS.register(instance);
    }

    // Replace MC's screens with our screens
    @SubscribeEvent
    public void onInitGuiEvent(GuiScreenEvent.InitGuiEvent.Post event) {
        // Main Menu
        if (event.getGui() instanceof MainMenuScreen) {
            boolean showFade;
            try {
                Field field = ObfuscationReflectionHelper.findField(MainMenuScreen.class, "showFadeInAnimation");
                field.setAccessible(true);
                showFade = field.getBoolean(event.getGui());
            } catch (Exception e) {
                e.printStackTrace();
                showFade = false;
            }
            Minecraft.getInstance().displayGuiScreen(new ClassicMainMenuScreen(Config.selectedVersion.get(), null, showFade));
        }

    }

    // Sends a debug message to console
    public static void debug(Object object) {
        if (Config.enableDebug.get()) {
            logger.info(object.toString());
        }
    }

    // Sends an error message to console
    public static void error(Exception e) {
        logger.throwing(e);
    }

    // Getters
    public static ClassicUI getInstance() {
        return instance;
    }
    public static Logger getLogger() {
        return logger;
    }

}
