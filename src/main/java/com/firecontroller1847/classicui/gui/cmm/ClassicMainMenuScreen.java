package com.firecontroller1847.classicui.gui.cmm;

import com.firecontroller1847.classicui.ClassicUI;
import com.firecontroller1847.classicui.Config;
import com.firecontroller1847.classicui.SupportedVersion;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Runnables;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.AccessibilityScreen;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.renderer.RenderSkybox;
import net.minecraft.client.renderer.RenderSkyboxCube;
import net.minecraft.client.resources.I18n;
import net.minecraft.realms.RealmsBridgeScreen;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.client.gui.screen.ModListScreen;
import net.minecraftforge.versions.forge.ForgeVersion;
import net.minecraftforge.versions.mcp.MCPVersion;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class ClassicMainMenuScreen extends Screen {

    // CMM Feature Sets
    private static final EnumSet<Feature> FS1_16 = EnumSet.of(
        Feature.FADE,
        Feature.PANORAMA,
        Feature.PANORAMA_OVERLAY,
        Feature.TITLE_MINECRAFT,
        Feature.TITLE_EDITION,
        Feature.TITLE_SPLASH,
        Feature.TITLE_FORGE,
        Feature.BUTTON_SINGLEPLAYER,
        Feature.BUTTON_MULTIPLAYER,
        Feature.BUTTON_MODS,
        Feature.BUTTON_REALMS,
        Feature.BUTTON_LANGUAGE,
        Feature.BUTTON_OPTIONS,
        Feature.BUTTON_QUIT,
        Feature.BUTTON_ACCESSIBILITY,
        Feature.NOTIFY_REALMS,
        Feature.NOTIFY_MOD_UPDATE,
        Feature.MULTIPLAYER_WARNING,
        Feature.COPYRIGHT_FORGE_VERSION,
        Feature.COPYRIGHT_GAME_VERSION,
        Feature.COPYRIGHT_MCP_VERSION,
        Feature.COPYRIGHT_MOD_COUNT,
        Feature.COPYRIGHT_FORGE_STATUS,
        Feature.COPYRIGHT_MOJANG_UNDERLINE
    );
    private static final EnumSet<Feature> FS1_7 = EnumSet.of(
        Feature.PANORAMA,
        Feature.PANORAMA_OVERLAY,
        Feature.TITLE_MINECRAFT,
        Feature.TITLE_SPLASH,
        Feature.BUTTON_SINGLEPLAYER,
        Feature.BUTTON_MULTIPLAYER,
        Feature.BUTTON_MODS,
        Feature.BUTTON_REALMS,
        Feature.BUTTON_LANGUAGE,
        Feature.BUTTON_OPTIONS,
        Feature.BUTTON_QUIT,
        Feature.COPYRIGHT_FORGE_VERSION,
        Feature.COPYRIGHT_GAME_VERSION,
        Feature.COPYRIGHT_MCP_VERSION,
        Feature.COPYRIGHT_MOD_COUNT
    );
    private static final EnumSet<Feature> FS1_2 = EnumSet.of(
        Feature.PANORAMA,
        Feature.PANORAMA_OVERLAY,
        Feature.TITLE_MINECRAFT,
        Feature.TITLE_SPLASH,
        Feature.BUTTON_SINGLEPLAYER,
        Feature.BUTTON_MULTIPLAYER,
        Feature.BUTTON_RESOURCEPACKS,
        Feature.BUTTON_MODS,
        Feature.BUTTON_LANGUAGE,
        Feature.BUTTON_OPTIONS,
        Feature.BUTTON_QUIT,
        Feature.COPYRIGHT_FORGE_VERSION,
        Feature.COPYRIGHT_GAME_VERSION,
        Feature.COPYRIGHT_MCP_VERSION,
        Feature.COPYRIGHT_MOD_COUNT
    );

    // CMM Variables
    private final SupportedVersion CURRENT_VERSION;
    private final EnumSet<Feature> CURRENT_FEATURE_SET;

    // Resources
    private static final RenderSkyboxCube RESOURCE_PANORAMA_SKYBOX_DEFAULT = new RenderSkyboxCube(new ResourceLocation("textures/gui/title/background/panorama"));
    private static final RenderSkyboxCube RESOURCE_PANORAMA_SKYBOX_OLD = new RenderSkyboxCube(new ResourceLocation(ClassicUI.MOD_ID, "textures/gui/title/background/panorama"));
    private static final ResourceLocation RESOURCE_PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
    private static final ResourceLocation RESOURCE_TITLE_MINECRAFT = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation RESOURCE_TITLE_EDITION = new ResourceLocation("textures/gui/title/edition.png");
    private static final ResourceLocation RESOURCE_ACCESSIBILITY_TEXTURES = new ResourceLocation("textures/gui/accessibility.png");

    // Copyright
    private int copyrightWidth;
    private int copyrightPos;

    // Fading
    private final boolean showFadeAnimation;
    private long firstRenderTime;

    // Variables
    private final RenderSkybox PANORAMA_DEFAULT = new RenderSkybox(RESOURCE_PANORAMA_SKYBOX_DEFAULT);
    private final RenderSkybox PANORAMA_OLD = new RenderSkybox(RESOURCE_PANORAMA_SKYBOX_OLD);
    private boolean showMisspelledTitleScreen;
    private String splash;

    // Constructor
    public ClassicMainMenuScreen(SupportedVersion version, @Nullable EnumSet<Feature> features, boolean showFadeAnimation) {
        super(new TranslationTextComponent("narrator.screen.title"));

        // CustomMainMenu Stuff
        this.CURRENT_VERSION = version;
        if (features == null) {
            if (version == SupportedVersion.R1_16) {
                features = FS1_16;
            } else if (version == SupportedVersion.R1_7) {
                features = FS1_7;
            } else if (version == SupportedVersion.R1_2) {
                features = FS1_2;
            } else {
                // If somehow there is an unimplemented supported version, use the latest
                ClassicUI.getLogger().error("Unimplemented supported version detected! Please contact the mod developer.");
                features = FS1_16;
            }
        }
        this.CURRENT_FEATURE_SET = features.clone();

        // Remove disabled features
        for (String feature : Config.fscmm.get()) {
            try {
                this.CURRENT_FEATURE_SET.remove(ClassicMainMenuScreen.Feature.valueOf(feature));
            } catch (Exception e) {
                // Forge failed to validate. Why!
            }
        }

        // MainMenu Stuff
        this.showMisspelledTitleScreen = (double) (new Random()).nextFloat() < 1.0e-4d;
        this.showFadeAnimation = isFeatureEnabled(Feature.FADE) && showFadeAnimation;
    }

    // Init
    @Override
    protected void init() {
        if (CURRENT_FEATURE_SET.contains(Feature.TITLE_SPLASH) && splash == null) {
            splash = minecraft.getSplashes().getSplashText();
        }

        // We are not connected to a realms server
        minecraft.setConnectedToRealms(false);

        // Calculate Copyright Position
        copyrightWidth = font.getStringWidth("Copyright Mojang AB. Do not distribute!");
        copyrightPos = width - copyrightWidth - 2;

        // Determine If Multiplayer Is Enabled
        boolean multiplayerEnabled = minecraft.isMultiplayerEnabled();
        Button.ITooltip multiplayerDisabledTooltip = multiplayerEnabled ? Button.field_238486_s_ : (button, matrixStack, mouseX, mouseY) -> {
            if (!button.active) {
                this.renderToolTip(matrixStack, minecraft.fontRenderer.trimStringToWidth(new TranslationTextComponent("title.multiplayer.disabled"), Math.max(this.width / 2 - 43, 170)), mouseX, mouseY, font);
            }
        };

        // Main Button Offsets
        int count = 0;
        int topOffset = height / 4 + 48;
        int leftOffset = width / 2 - 100;

        // Singleplayer Button
        if (isFeatureEnabled(Feature.BUTTON_SINGLEPLAYER)) {
            this.addButton(new Button(leftOffset, topOffset + (count++ * 24), 200, 20, new TranslationTextComponent("menu.singleplayer"), (button) -> {
                minecraft.displayGuiScreen(new WorldSelectionScreen(this));
            }));
        }

        // Multiplayer Button
        if (isFeatureEnabled(Feature.BUTTON_MULTIPLAYER)) {
            this.addButton(new Button(leftOffset, topOffset + (count++ * 24), 200, 20, new TranslationTextComponent("menu.multiplayer"), (button) -> {
                Screen screen;
                if (minecraft.gameSettings.skipMultiplayerWarning) {
                    screen = new MultiplayerScreen(this);
                } else {
                    screen = new MultiplayerWarningScreen(this);
                }
                minecraft.displayGuiScreen(screen);
            }, multiplayerDisabledTooltip)).active = multiplayerEnabled;
        }

        // Mods, ResourcePacks & Realms Buttons
        boolean left = CURRENT_VERSION == SupportedVersion.R1_2 ? isFeatureEnabled(Feature.BUTTON_RESOURCEPACKS) : (CURRENT_VERSION == SupportedVersion.R1_7 ? isFeatureEnabled(Feature.BUTTON_REALMS) : isFeatureEnabled(Feature.BUTTON_MODS));
        boolean right = CURRENT_VERSION == SupportedVersion.R1_7 || CURRENT_VERSION == SupportedVersion.R1_2 ? isFeatureEnabled(Feature.BUTTON_MODS) : isFeatureEnabled(Feature.BUTTON_REALMS);
        boolean split = left && right;
        if (left) {
            if (CURRENT_VERSION == SupportedVersion.R1_2) {
                this.addButton(new Button(leftOffset, topOffset + (split ? count * 24 : count++ * 24), split ? 98 : 200, 20, new StringTextComponent(I18n.format("options.resourcepack").replace("...", "")), (button) -> {
                    minecraft.displayGuiScreen(new PackScreen(this, minecraft.getResourcePackList(), resourcePackList -> {
                        // I tried doing this by creating a new OptionsScreen and reflectively calling the method,
                        // but it didn't like that, so we get this and you're going to be happy about it
                        List<String> list = ImmutableList.copyOf(minecraft.gameSettings.resourcePacks);
                        minecraft.gameSettings.resourcePacks.clear();
                        minecraft.gameSettings.incompatibleResourcePacks.clear();
                        for (ResourcePackInfo resourcePackInfo : resourcePackList.getEnabledPacks()) {
                            if (!resourcePackInfo.isOrderLocked()) {
                                minecraft.gameSettings.resourcePacks.add(resourcePackInfo.getName());
                                if (!resourcePackInfo.getCompatibility().isCompatible()) {
                                    minecraft.gameSettings.incompatibleResourcePacks.add(resourcePackInfo.getName());
                                }
                            }
                        }
                        minecraft.gameSettings.saveOptions();
                        List<String> list1 = ImmutableList.copyOf(minecraft.gameSettings.resourcePacks);
                        if (!list1.equals(list)) {
                            minecraft.reloadResources(); // Forge, what is FMLClientHandler?
                        }
                    }, minecraft.getFileResourcePacks(), new TranslationTextComponent("resourcePack.title")));
                }));
            } else if (CURRENT_VERSION == SupportedVersion.R1_7) {
                this.addButton(new Button(leftOffset, topOffset + (split ? count * 24 : count++ * 24), split ? 98 : 200, 20, new TranslationTextComponent("menu.online"), (button) -> {
                    (new RealmsBridgeScreen()).func_231394_a_(this);
                }, multiplayerDisabledTooltip)).active = multiplayerEnabled;
            } else {
                this.addButton(new Button(leftOffset, topOffset + (split ? count * 24 : count++ * 24), split ? 98 : 200, 20, new TranslationTextComponent("fml.menu.mods"), button -> {
                    minecraft.displayGuiScreen(new ModListScreen(this));
                }));
            }
        }
        if (right) {
            if (CURRENT_VERSION == SupportedVersion.R1_7 || CURRENT_VERSION == SupportedVersion.R1_2) {
                this.addButton(new Button(leftOffset + (split ? 102 : 0), topOffset + (count++ * 24), split ? 98 : 200, 20, new TranslationTextComponent("fml.menu.mods"), button -> {
                    minecraft.displayGuiScreen(new ModListScreen(this));
                }));
            } else {
                this.addButton(new Button(leftOffset + (split ? 102 : 0), topOffset + (count++ * 24), split ? 98 : 200, 20, new TranslationTextComponent("menu.online"), (button) -> {
                    (new RealmsBridgeScreen()).func_231394_a_(this);
                }, multiplayerDisabledTooltip)).active = multiplayerEnabled;
            }
        }

        // TODO: Realms notifications
        // TODO: Mod updates notifications

        // Big Break
        topOffset += 12;

        // Language Button
        if (isFeatureEnabled(Feature.BUTTON_LANGUAGE)) {
            this.addButton(new ImageButton(leftOffset - 24, topOffset + (count * 24), 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, (button) -> {
                minecraft.displayGuiScreen(new LanguageScreen(this, minecraft.gameSettings, minecraft.getLanguageManager()));
            }, new TranslationTextComponent("narrator.button.language")));
        }

        // Options & Quit Game Buttons
        left = isFeatureEnabled(Feature.BUTTON_OPTIONS);
        right = isFeatureEnabled(Feature.BUTTON_QUIT);
        split = left && right;
        if (left) {
            this.addButton(new Button(leftOffset, topOffset + (count * 24), split ? 98 : 200, 20, new TranslationTextComponent("menu.options"), (button) -> {
                minecraft.displayGuiScreen(new OptionsScreen(this, minecraft.gameSettings));
            }));
        }
        if (right) {
            this.addButton(new Button(leftOffset + (split ? 102 : 0), topOffset + (count * 24), split ? 98 : 200, 20, new TranslationTextComponent("menu.quit"), (button) -> {
                minecraft.shutdown();
            }));
        }

        // Accessibility Button
        if (isFeatureEnabled(Feature.BUTTON_ACCESSIBILITY)) {
            this.addButton(new ImageButton(leftOffset + 204, topOffset + (count * 24), 20, 20, 0, 0, 20, RESOURCE_ACCESSIBILITY_TEXTURES, 32, 64, (button) -> {
                minecraft.displayGuiScreen(new AccessibilityScreen(this, minecraft.gameSettings));
            }, new TranslationTextComponent("narrator.button.accessibility")));
        }
    }

    // Render
    @Override
    @ParametersAreNonnullByDefault
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Update First Render Time for Fading
        if (firstRenderTime == 0L && showFadeAnimation) {
            firstRenderTime = Util.milliTime();
        }

        // Determine Alpha Amount
        float floatAlpha = showFadeAnimation ? (float) (Util.milliTime() - firstRenderTime) / 1000.0f : 1.0f;

        // Blank Matrix
        fill(matrixStack, 0, 0, width, height, -1);

        // Render Panorama
        if (isFeatureEnabled(Feature.PANORAMA)) {
            if (CURRENT_VERSION == SupportedVersion.R1_2 || CURRENT_VERSION == SupportedVersion.R1_7) {
                PANORAMA_OLD.render(partialTicks, MathHelper.clamp(floatAlpha, 0.0f, 1.0f));
            } else {
                PANORAMA_DEFAULT.render(partialTicks, MathHelper.clamp(floatAlpha, 0.0f, 1.0f));
            }
        }

        // Render Panorama Overlay
        if (isFeatureEnabled(Feature.PANORAMA_OVERLAY)) {
            minecraft.getTextureManager().bindTexture(RESOURCE_PANORAMA_OVERLAY);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, showFadeAnimation ? (float) MathHelper.ceil(MathHelper.clamp(floatAlpha, 0.0f, 1.0f)) : 1.0f);
            AbstractGui.blit(matrixStack, 0, 0, width, height, 0.0f, 0.0f, 16, 128, 16, 128);
        }

        // Prepare Hexadecimal Alpha for Fade
        float floatAlphaMinusOne = showFadeAnimation ? MathHelper.clamp(floatAlpha - 1.0f, 0.0f, 1.0f) : 1.0f;
        int hexAlpha = MathHelper.ceil(floatAlphaMinusOne * 255.0f) << 24;
        if ((hexAlpha & -0x4000000) == 0) { // Wait for the full fade animation
            return;
        }

        // Fade
        GL11.glColor4f(1.0f, 1.0f, 1.0f, floatAlphaMinusOne);

        // Draw Minecraft Text
        if (isFeatureEnabled(Feature.TITLE_MINECRAFT)) {
            minecraft.getTextureManager().bindTexture(RESOURCE_TITLE_MINECRAFT);
            int titleLeftOffset = width / 2 - 137;
            if (CURRENT_VERSION == SupportedVersion.R1_7 || CURRENT_VERSION == SupportedVersion.R1_2) {
                if (showMisspelledTitleScreen) {
                    this.blit(matrixStack, titleLeftOffset, 30, 0, 0, 99, 44);
                    this.blit(matrixStack, titleLeftOffset + 99, 30, 129, 0, 27, 44);
                    this.blit(matrixStack, titleLeftOffset + 99 + 26, 30, 126, 0, 3, 44);
                    this.blit(matrixStack, titleLeftOffset + 99 + 26 + 3, 30, 99, 0, 26, 44);
                    this.blit(matrixStack, titleLeftOffset + 155, 30, 0, 45, 155, 44);
                } else {
                    this.blit(matrixStack, titleLeftOffset, 30, 0, 0, 155, 44);
                    this.blit(matrixStack, titleLeftOffset + 155, 30, 0, 45, 155, 44);
                }
            } else {
                if (showMisspelledTitleScreen) {
                    this.blitBlackOutline(titleLeftOffset, 30, (x, y) -> {
                        this.blit(matrixStack, x, y, 0, 0, 99, 44);
                        this.blit(matrixStack, x + 99, y, 129, 0, 27, 44);
                        this.blit(matrixStack, x + 99 + 26, y, 126, 0, 3, 44);
                        this.blit(matrixStack, x + 99 + 26 + 3, y, 99, 0, 26, 44);
                        this.blit(matrixStack, x + 155, y, 0, 45, 155, 44);
                    });
                } else {
                    this.blitBlackOutline(titleLeftOffset, 30, (x, y) -> {
                        this.blit(matrixStack, x, y, 0, 0, 155, 44);
                        this.blit(matrixStack, x + 155, y, 0, 45, 155, 44);
                    });
                }
            }
        }

        // Draw Edition Text
        if (isFeatureEnabled(Feature.TITLE_EDITION)) {
            minecraft.getTextureManager().bindTexture(RESOURCE_TITLE_EDITION);
            AbstractGui.blit(matrixStack, this.width / 2 - 49, 67, 0.0f, 0.0f, 98, 14, 128, 16);
        }

        // Draw Minecraft Forge Text
        if (isFeatureEnabled(Feature.TITLE_FORGE)) {
            VersionChecker.Status status = ForgeVersion.getStatus();
            if (status == VersionChecker.Status.BETA || status == VersionChecker.Status.BETA_OUTDATED) {
                ITextComponent line1 = new TranslationTextComponent("forge.update.beta.1", TextFormatting.RED, TextFormatting.RESET).mergeStyle(TextFormatting.RED);
                AbstractGui.drawCenteredString(matrixStack, font, line1, width / 2, 4, 0xFFFFFF | hexAlpha);

                ITextComponent line2 = new TranslationTextComponent("forge.update.beta.2");
                AbstractGui.drawCenteredString(matrixStack, font, line2, width / 2, 4 + (font.FONT_HEIGHT + 1), 0xFFFFFF | hexAlpha);

                ForgeHooksClient.forgeStatusLine = I18n.format("forge.update.newversion", ForgeVersion.getTarget());
            }
        }

        // Draw Splash Text
        if (isFeatureEnabled(Feature.TITLE_SPLASH) && this.splash != null) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) (width / 2 + 90), 70.0f, 0.0f);
            GL11.glRotatef(-20.0f, 0.0f, 0.0f, 1.0f);

            float scale = 1.8f - MathHelper.abs(MathHelper.sin((float) (Util.milliTime() % 1000L) / 1000.0f * ((float) Math.PI * 2.0f)) * 0.1f);
            scale *= 100.0f / (float) (font.getStringWidth(splash) + 32);
            GL11.glScalef(scale, scale, scale);

            AbstractGui.drawCenteredString(matrixStack,font, splash, 0, -8, 0xFFFF00 | hexAlpha);

            GL11.glPopMatrix();
        }

        // Draw Copyright (Left Side)
        int leftLineCount = 0;
        if (CURRENT_VERSION == SupportedVersion.R1_7 || CURRENT_VERSION == SupportedVersion.R1_2) {
            if (CURRENT_VERSION == SupportedVersion.R1_7) {
                if (isFeatureEnabled(Feature.COPYRIGHT_MOD_COUNT)) {
                    AbstractGui.drawString(matrixStack, font, ModList.get().size() + " mods loaded, " + ModList.get().size() + " mods active", 2, this.height - (10 + leftLineCount++ * (font.FONT_HEIGHT + 1)), 0xFFFFFF | hexAlpha);
                }
            } else {
                if (isFeatureEnabled(Feature.COPYRIGHT_MOD_COUNT)) {
                    AbstractGui.drawString(matrixStack, font, ModList.get().size() + " mods loaded", 2, this.height - (10 + leftLineCount++ * (font.FONT_HEIGHT + 1)), 0xFFFFFF | hexAlpha);
                }
            }
            if (isFeatureEnabled(Feature.COPYRIGHT_FORGE_VERSION)) {
                AbstractGui.drawString(matrixStack, font, "Minecraft Forge " + ForgeVersion.getVersion(), 2, this.height - (10 + leftLineCount++ * (font.FONT_HEIGHT + 1)), 0xFFFFFF | hexAlpha);
            }
            if (isFeatureEnabled(Feature.COPYRIGHT_MCP_VERSION)) {
                AbstractGui.drawString(matrixStack, font, "MCP " + MCPVersion.getMCPVersion(), 2, this.height - (10 + leftLineCount++ * (font.FONT_HEIGHT + 1)), 0xFFFFFF | hexAlpha);
            }
            if (isFeatureEnabled(Feature.COPYRIGHT_GAME_VERSION)) {
                AbstractGui.drawString(matrixStack, font, "Minecraft " + MCPVersion.getMCVersion(), 2, this.height - (10 + leftLineCount++ * (font.FONT_HEIGHT + 1)), 0xFFFFFF | hexAlpha);
            }
        } else {
            if (isFeatureEnabled(Feature.COPYRIGHT_MOD_COUNT)) {
                AbstractGui.drawString(matrixStack, font, ModList.get().size() + " mods loaded", 2, this.height - (10 + leftLineCount++ * (font.FONT_HEIGHT + 1)), 0xFFFFFF | hexAlpha);
            }
            if (isFeatureEnabled(Feature.COPYRIGHT_MCP_VERSION)) {
                AbstractGui.drawString(matrixStack, font, "MCP " + MCPVersion.getMCPVersion(), 2, this.height - (10 + leftLineCount++ * (font.FONT_HEIGHT + 1)), 0xFFFFFF | hexAlpha);
            }
            if (isFeatureEnabled(Feature.COPYRIGHT_GAME_VERSION)) {
                AbstractGui.drawString(matrixStack, font, "Minecraft " + MCPVersion.getMCVersion(), 2, this.height - (10 + leftLineCount++ * (font.FONT_HEIGHT + 1)), 0xFFFFFF | hexAlpha);
            }
            if (isFeatureEnabled(Feature.COPYRIGHT_FORGE_VERSION)) {
                AbstractGui.drawString(matrixStack, font, "Forge " + ForgeVersion.getVersion(), 2, this.height - (10 + leftLineCount++ * (font.FONT_HEIGHT + 1)), 0xFFFFFF | hexAlpha);
            }
        }

        // Draw Copyright (Right Side)
        int rightLineCount = 0;
        AbstractGui.drawString(matrixStack, font, "Copyright Mojang AB. Do not distribute!", copyrightPos, height - (10 + rightLineCount++ * (font.FONT_HEIGHT + 1)), 0xFFFFFF | hexAlpha);
        if (isFeatureEnabled(Feature.COPYRIGHT_MOJANG_UNDERLINE)) {
            if (mouseX > copyrightPos && mouseX < copyrightPos + copyrightWidth && mouseY > height - 10 && mouseY < height) {
                AbstractGui.fill(matrixStack, copyrightPos, height - 1, copyrightPos + copyrightWidth, height, 0xFFFFFF | hexAlpha);
            }
        }
        if (isFeatureEnabled(Feature.COPYRIGHT_FORGE_STATUS)) {
            AbstractGui.drawString(matrixStack, font, ForgeHooksClient.forgeStatusLine, width - font.getStringWidth(ForgeHooksClient.forgeStatusLine), height - (10 + rightLineCount++ * (font.FONT_HEIGHT + 1)), 0xFFFFFF | hexAlpha);
        }

        // Set Button Alphas
        for (Widget widget : buttons) {
            widget.setAlpha(floatAlphaMinusOne);
        }

        // Call Super
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        // TODO: Realms notifications
        // TODO: Mod updates notifications
    }

    // Mouse Clicked
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        } else {
            if (mouseX > copyrightPos && mouseX < copyrightPos + copyrightWidth && mouseY > height - 10 && mouseY < height) {
                minecraft.displayGuiScreen(new WinGameScreen(false, Runnables.doNothing()));
            }
            return false;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return Config.enableDebug.get();
    }

    private boolean isFeatureEnabled(Feature feature) {
        return CURRENT_FEATURE_SET.contains(feature);
    }

    // Feature Set
    public enum Feature {

        // Fade
        FADE,

        // Panorama
        PANORAMA,
        PANORAMA_OVERLAY,

        // Title
        TITLE_MINECRAFT,
        TITLE_EDITION,
        TITLE_SPLASH,
        TITLE_FORGE,

        // Buttons
        BUTTON_SINGLEPLAYER,
        BUTTON_MULTIPLAYER,
        BUTTON_RESOURCEPACKS,
        BUTTON_MODS,
        BUTTON_REALMS,
        BUTTON_LANGUAGE,
        BUTTON_OPTIONS,
        BUTTON_QUIT,
        BUTTON_ACCESSIBILITY,

        // Notifications
        NOTIFY_REALMS,
        NOTIFY_MOD_UPDATE,

        // Multiplayer Warning
        MULTIPLAYER_WARNING,

        // Copyright Information
        COPYRIGHT_FORGE_VERSION,
        COPYRIGHT_GAME_VERSION,
        COPYRIGHT_MCP_VERSION,
        COPYRIGHT_MOD_COUNT,
        COPYRIGHT_FORGE_STATUS,
        COPYRIGHT_MOJANG_UNDERLINE

    }

}
