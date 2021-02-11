package com.firecontroller1847.classicui.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderSkybox;
import net.minecraft.client.renderer.RenderSkyboxCube;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.versions.forge.ForgeVersion;
import org.lwjgl.opengl.GL11;

public class ClassicMainMenuScreenOld extends Screen {

    // Resources
    private static final RenderSkyboxCube PANORAMA_RESOURCES = new RenderSkyboxCube(new ResourceLocation("textures/gui/title/background/panorama"));
    private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
    private static final ResourceLocation MINECRAFT_TITLE = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation MINECRAFT_TITLE_EDITION = new ResourceLocation("textures/gui/title/edition.png");

    // Objects
    private final RenderSkybox panorama = new RenderSkybox(PANORAMA_RESOURCES);
    private String splashText;

    // Constructor
    public ClassicMainMenuScreenOld() {
        super(new TranslationTextComponent("narrator.screen.title"));
    }

    @Override
    public void init() {
        // Generate Splash Text
        if (this.splashText == null) {
            this.splashText = this.minecraft.getSplashes().getSplashText();
            System.out.println("SPLASH TEXT: " + this.splashText);
        }

        int j = this.height / 4 + 48;
        this.addButton(new Button(this.width / 2 - 100, j + 72 + 12, 98, 20, new TranslationTextComponent("menu.options"), (p_213096_1_) -> {
            this.minecraft.displayGuiScreen(new MainMenuScreen());
        }));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Offsets
        int leftOffset = this.width / 2 - 137;

        // Panorama
        fill(matrixStack, 0, 0, this.width, this.height, -1);
        this.panorama.render(partialTicks, 1.0f);

        // Panorama Overlay
        this.minecraft.getTextureManager().bindTexture(PANORAMA_OVERLAY);
        blit(matrixStack, 0, 0, this.width, this.height, 0.0f, 0.0f, 16, 128, 16, 128);

        // Title Screen
        this.minecraft.getTextureManager().bindTexture(MINECRAFT_TITLE);
        this.blitBlackOutline(leftOffset, 30, (x, y) -> {
            this.blit(matrixStack, x, y, 0, 0, 155, 44);
            this.blit(matrixStack, x + 155, y, 0, 45, 155, 44);
        });

        // Edition
        this.minecraft.getTextureManager().bindTexture(MINECRAFT_TITLE_EDITION);
        blit(matrixStack, leftOffset + 88, 67, 0.0f, 0.0f, 98, 14, 128, 16);

        // Minecraft Forge
        this.renderForge(matrixStack, width, height);

        // Splash
        if (this.splashText != null) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)(this.width / 2 + 90), 70.0F, 0.0F);
            GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
            float scale = 1.8F - MathHelper.abs(MathHelper.sin((float)(Util.milliTime() % 1000L) / 1000.0F * ((float)Math.PI * 2F)) * 0.1F);
            scale = scale * 100.0F / (float)(this.font.getStringWidth(this.splashText) + 32);
            GL11.glScalef(scale, scale, scale);
            drawCenteredString(matrixStack, this.font, this.splashText, 0, -8, -256);
            GL11.glPopMatrix();
        }

        // Minecraft Version
        String s = "Minecraft " + SharedConstants.getVersion().getName();
        if (this.minecraft.isDemo()) {
            s = s + " Demo";
        } else {
            s = s + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
        }
        if (this.minecraft.isModdedClient()) {
            s = s + I18n.format("menu.modded");
        }

        net.minecraftforge.fml.BrandingControl.forEachLine(true, true, (brdline, brd) ->
            drawString(matrixStack, this.font, brd, 2, this.height - ( 10 + brdline * (this.font.FONT_HEIGHT + 1)), 255)
        );

        net.minecraftforge.fml.BrandingControl.forEachAboveCopyrightLine((brdline, brd) ->
            drawString(matrixStack, this.font, brd, this.width - font.getStringWidth(brd), this.height - (10 + (brdline + 1) * ( this.font.FONT_HEIGHT + 1)), 255)
        );

        // Super
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private void renderForge(MatrixStack matrixStack, int width, int height) {
        // Version Checker
        VersionChecker.Status status = ForgeVersion.getStatus();
        if (status == VersionChecker.Status.BETA || status == VersionChecker.Status.BETA_OUTDATED) {
            // Line 1
            ITextComponent line = new TranslationTextComponent("forge.update.beta.1", TextFormatting.RED, TextFormatting.RESET).mergeStyle(TextFormatting.RED);
            AbstractGui.drawCenteredString(matrixStack, font, line, width / 2, 4, -1);

            // Line 2
            line = new TranslationTextComponent("forge.update.beta.2");
            AbstractGui.drawCenteredString(matrixStack, font, line, width / 2, 4 + (font.FONT_HEIGHT + 1), -1);
        }

        String line = null;
        if (status == VersionChecker.Status.OUTDATED || status == VersionChecker.Status.BETA_OUTDATED) {
            line = I18n.format("forge.update.newversion", ForgeVersion.getTarget());
        }
        ForgeHooksClient.forgeStatusLine = line;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void closeScreen() {
        this.minecraft.displayGuiScreen(new OptionsScreen(new MainMenuScreen(), this.minecraft.gameSettings));
    }
}
