package com.replaymod.extras;

import com.replaymod.LiteModReplayMod;
import com.replaymod.extras.ducks.IEntityRenderer;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.gui.overlay.GuiReplayOverlay;
import de.johni0702.minecraft.gui.element.GuiImage;
import de.johni0702.minecraft.gui.element.IGuiImage;
import de.johni0702.minecraft.gui.layout.HorizontalLayout;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;

public class FullBrightness implements Extra {
    private ReplayModReplay module;

    private final IGuiImage indicator = new GuiImage().setTexture(LiteModReplayMod.TEXTURE, 90, 20, 19, 13).setSize(19, 13);

    private GameSettings gameSettings;
    private boolean active;
    private float originalGamma;

    @Override
    public void register(final LiteModReplayMod mod) throws Exception {
        this.module = ReplayModReplay.instance;
        this.gameSettings = mod.getMinecraft().gameSettings;

        mod.getKeyBindingRegistry().registerKeyBinding("replaymod.input.lighting", Keyboard.KEY_Z, () -> {
            active = !active;
            ((IEntityRenderer) mod.getMinecraft().entityRenderer).setLightmapUpdateNeeded(true);
            ReplayHandler replayHandler = module.getReplayHandler();
            if (replayHandler != null) {
                updateIndicator(replayHandler.getOverlay());
            }
        });
    }

    @Override
    public void beginTick() {
        if (active && module.getReplayHandler() != null) {
            originalGamma = gameSettings.gammaSetting;
            gameSettings.gammaSetting = 1000;
        }
    }

    @Override
    public void endTick() {
        if (active && module.getReplayHandler() != null) {
            gameSettings.gammaSetting = originalGamma;
        }
    }

    @Override
    public void postReplayOpened(ReplayHandler handler) {
        updateIndicator(handler.getOverlay());
    }

    private void updateIndicator(GuiReplayOverlay overlay) {
        if (active) {
            overlay.statusIndicatorPanel.addElements(new HorizontalLayout.Data(1), indicator);
        } else {
            overlay.statusIndicatorPanel.removeElement(indicator);
        }
    }
}
