package com.replaymod.recording.gui;

import com.replaymod.core.SettingsRegistry;
import com.replaymod.recording.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import static com.replaymod.LiteModReplayMod.TEXTURE;
import static com.replaymod.LiteModReplayMod.TEXTURE_SIZE;

/**
 * Renders overlay during recording.
 */
public class GuiRecordingOverlay {
    private final Minecraft mc;
    private final SettingsRegistry settingsRegistry;
    private boolean registered;

    public GuiRecordingOverlay(Minecraft mc, SettingsRegistry settingsRegistry) {
        this.mc = mc;
        this.settingsRegistry = settingsRegistry;
    }

    public void register() {
        registered = true;
    }

    public void unregister() {
        registered = false;
    }

    /**
     * Render the recording icon and text in the top left corner of the screen.
     * @param event Rendered post game overlay
     */
    public void renderRecordingIndicator() {
        if (!registered)
            return;

        if (settingsRegistry.get(Setting.INDICATOR)) {
            FontRenderer fontRenderer = mc.fontRenderer;
            fontRenderer.drawString(I18n.format("replaymod.gui.recording").toUpperCase(), 30, 18 - (fontRenderer.FONT_HEIGHT / 2), 0xffffffff);
            mc.getTextureManager().bindTexture(TEXTURE);
            GlStateManager.resetColor();
            GlStateManager.enableAlpha();
            Gui.drawModalRectWithCustomSizedTexture(10, 10, 58, 20, 16, 16, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }
}
