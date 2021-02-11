package com.firecontroller1847.classicui.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.AccessibilityScreen;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.renderer.RenderSkybox;
import net.minecraft.client.renderer.RenderSkyboxCube;
import net.minecraft.client.resources.I18n;
import net.minecraft.realms.RealmsBridgeScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.BrandingControl;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.client.gui.screen.ModListScreen;
import net.minecraftforge.versions.forge.ForgeVersion;
import org.lwjgl.opengl.GL11;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

public class ClassicMainMenuScreen extends Screen {

    // Resources
    private static final RenderSkyboxCube PANORAMA_SKYBOX = new RenderSkyboxCube(new ResourceLocation("textures/gui/title/background/panorama"));
    private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
    private static final ResourceLocation TITLE_MINECRAFT = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation TITLE_EDITION = new ResourceLocation("textures/gui/title/edition.png");
    private static final ResourceLocation ACCESSIBILITY_TEXTURES = new ResourceLocation("textures/gui/accessibility.png");

    // Copyright
    private int copyrightWidth;
    private int copyrightWidthPos;

    // Variables
    private final RenderSkybox panorama = new RenderSkybox(PANORAMA_SKYBOX);
    private String splash;
    private boolean showMisspelledTitleScreen;

    // Constructors
    public ClassicMainMenuScreen() {
        super(new TranslationTextComponent("narrator.screen.title"));
        this.showMisspelledTitleScreen = (double) (new Random()).nextFloat() < 1.0e-4d;
    }

    // Init
    @Override
    protected void init() {
        // Generate Splash
        if (splash == null) {
            splash = minecraft.getSplashes().getSplashText();
        }

        // We are not connected to a realms server
        minecraft.setConnectedToRealms(false);

        // Calculate Copyright Position
        copyrightWidth = font.getStringWidth("Copyright Mojang AB. Do not distribute!");
        copyrightWidthPos = width - copyrightWidth - 2;

        // Main Button Offsets
        int topOffset = height / 4 + 48;

        // Singleplayer & Multiplayer
        // TODO: Demo buttons... wait can you even mod a demo?
        this.addButton(new Button(width / 2 - 100, topOffset, 200, 20, new TranslationTextComponent("menu.singleplayer"), (button) -> {
            minecraft.displayGuiScreen(new WorldSelectionScreen(this));
        }));
        boolean multiplayerEnabled = minecraft.isMultiplayerEnabled();
        Button.ITooltip multiplayerDisabledTooltip = multiplayerEnabled ? Button.field_238486_s_ : (button, matrixStack, mouseX, mouseY) -> {
            if (!button.active) {
                this.renderToolTip(matrixStack, minecraft.fontRenderer.trimStringToWidth(new TranslationTextComponent("title.multiplayer.disabled"), Math.max(this.width / 2 - 43, 170)), mouseX, mouseY, font);
            }
        };
        this.addButton(new Button(width / 2 - 100, topOffset + 24, 200, 20, new TranslationTextComponent("menu.multiplayer"), (button) -> {
            Screen screen;
            if (minecraft.gameSettings.skipMultiplayerWarning) {
                screen = new MultiplayerScreen(this);
            } else {
                screen = new MultiplayerWarningScreen(this);
            }
            minecraft.displayGuiScreen(screen);
        }, multiplayerDisabledTooltip)).active = multiplayerEnabled;

        // Mods & Minecraft Realms
        // TODO: Mod update notifications
        this.addButton(new Button(width / 2 - 100, topOffset + 24 * 2, 98, 20, new TranslationTextComponent("fml.menu.mods"), button -> {
            minecraft.displayGuiScreen(new ModListScreen(this));
        }));
        // TODO: Realms notifications
        this.addButton(new Button(width / 2 + 2, topOffset + 24 * 2, 98, 20, new TranslationTextComponent("menu.online"), (button) -> {
            (new RealmsBridgeScreen()).func_231394_a_(this);
        }, multiplayerDisabledTooltip)).active = multiplayerEnabled;

        // Language Button
        this.addButton(new ImageButton(width / 2 - 124, topOffset + 72 + 12, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, (button) -> {
            minecraft.displayGuiScreen(new LanguageScreen(this, minecraft.gameSettings, minecraft.getLanguageManager()));
        }, new TranslationTextComponent("narrator.button.language")));

        // Options & Quit
        this.addButton(new Button(width / 2 - 100, topOffset + 72 + 12, 98, 20, new TranslationTextComponent("menu.options"), (button) -> {
            minecraft.displayGuiScreen(new OptionsScreen(this, minecraft.gameSettings));
        }));
        this.addButton(new Button(width / 2 + 2, topOffset + 72 + 12, 98, 20, new TranslationTextComponent("menu.quit"), (button) -> {
            minecraft.shutdown();
        }));

        // Accessibility Button
        this.addButton(new ImageButton(width / 2 + 104, topOffset + 72 + 12, 20, 20, 0, 0, 20, ACCESSIBILITY_TEXTURES, 32, 64, (button) -> {
            minecraft.displayGuiScreen(new AccessibilityScreen(this, minecraft.gameSettings));
        }, new TranslationTextComponent("narrator.button.accessibility")));
    }

    // Render
    @Override
    @ParametersAreNonnullByDefault
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // TODO: Fading

        // Blank Matrix
        fill(matrixStack, 0, 0, width, height, -1);

        // Render Panorama
        panorama.render(partialTicks, 1.0f);

        // Render Panorama Overlay
        minecraft.getTextureManager().bindTexture(PANORAMA_OVERLAY);
        AbstractGui.blit(matrixStack, 0, 0, width, height, 0.0f, 0.0f, 16, 128, 16, 128);

        // Draw Title
        minecraft.getTextureManager().bindTexture(TITLE_MINECRAFT);
        if (showMisspelledTitleScreen) {
            this.blitBlackOutline(width / 2 - 137, 30, (x, y) -> {
                this.blit(matrixStack, x, y, 0, 0, 99, 44);
                this.blit(matrixStack, x + 99, y, 129, 0, 27, 44);
                this.blit(matrixStack, x + 99 + 26, y, 126, 0, 3, 44);
                this.blit(matrixStack, x + 99 + 26 + 3, y, 99, 0, 26, 44);
                this.blit(matrixStack, x + 155, y, 0, 45, 155, 44);
            });
        } else {
            this.blitBlackOutline(width / 2 - 137, 30, (x, y) -> {
                this.blit(matrixStack, x, y, 0, 0, 155, 44);
                this.blit(matrixStack, x + 155, y, 0, 45, 155, 44);
            });
        }

        // Draw Edition
        minecraft.getTextureManager().bindTexture(TITLE_EDITION);
        AbstractGui.blit(matrixStack, this.width / 2 - 49, 67, 0.0f, 0.0f, 98, 14, 128, 16);

        // Minecraft Forge
        VersionChecker.Status status = ForgeVersion.getStatus();
        if (status == VersionChecker.Status.BETA || status == VersionChecker.Status.BETA_OUTDATED) {
            ITextComponent line1 = new TranslationTextComponent("forge.update.beta.1", TextFormatting.RED, TextFormatting.RESET).mergeStyle(TextFormatting.RED);
            AbstractGui.drawCenteredString(matrixStack, font, line1, width / 2, 4, -1);

            ITextComponent line2 = new TranslationTextComponent("forge.update.beta.2");
            AbstractGui.drawCenteredString(matrixStack, font, line2, width / 2, 4 + (font.FONT_HEIGHT + 1), -1);

            ForgeHooksClient.forgeStatusLine = I18n.format("forge.update.newversion", ForgeVersion.getTarget());
        }

        // Splash
        if (this.splash != null) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) (width / 2 + 90), 70.0f, 0.0f);
            GL11.glRotatef(-20.0f, 0.0f, 0.0f, 1.0f);

            float scale = 1.8f - MathHelper.abs(MathHelper.sin((float) (Util.milliTime() % 1000L) / 1000.0f * ((float) Math.PI * 2.0f)) * 0.1f);
            scale *= 100.0f / (float) (font.getStringWidth(splash) + 32);
            GL11.glScalef(scale, scale, scale);

            AbstractGui.drawCenteredString(matrixStack,font, splash, 0, -8, 0xFFFF00);

            GL11.glPopMatrix();
        }

        // Left Branding
        BrandingControl.forEachLine(true, true, (line, branding) -> {
            AbstractGui.drawString(matrixStack, font, branding, 2, this.height - (10 + line * (font.FONT_HEIGHT + 1)), 0xFFFFFF);
        });

        // Right Branding
        BrandingControl.forEachAboveCopyrightLine((line, branding) -> {
            AbstractGui.drawString(matrixStack, font, branding, width - font.getStringWidth(branding), height - (10 + (line + 1) * (font.FONT_HEIGHT + 1)), 0xFFFFFF);
        });

        // Draw Copyright
        AbstractGui.drawString(matrixStack, font, "Copyright Mojang AB. Do not distribute!", copyrightWidthPos, height - 10, 0xFFFFFF);
        if (mouseX > copyrightWidth && mouseX < copyrightWidthPos + copyrightWidth && mouseY > height - 10 && mouseY < height) {
            AbstractGui.fill(matrixStack, copyrightWidthPos, height - 1, copyrightWidthPos + copyrightWidth, height, 0xFFFFFF);
        }

        // TODO: Realms notifications
        // TODO: Mod update notifications

        // Call Super
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

}
