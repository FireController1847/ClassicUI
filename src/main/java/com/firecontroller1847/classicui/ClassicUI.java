package com.firecontroller1847.classicui;

import com.firecontroller1847.classicui.gui.ClassicMainMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    }

    // Register Events
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(instance);
    }

    // Replace MC's screens with our screens
    @SubscribeEvent
    public void onInitGuiEvent(GuiScreenEvent.InitGuiEvent.Post event) {
        System.out.println(event.getGui());
        if (event.getGui() instanceof MainMenuScreen) {
            Minecraft.getInstance().displayGuiScreen(new ClassicMainMenuScreen());
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

}
