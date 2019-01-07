package com.replaymod.simplepathing.preview;

import com.replaymod.LiteModReplayMod;
import com.replaymod.core.SettingsRegistry;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.simplepathing.ReplayModSimplePathing;
import com.replaymod.simplepathing.Setting;
import org.lwjgl.input.Keyboard;

public class PathPreview {
    private final ReplayModSimplePathing mod;

    private ReplayHandler replayHandler;
    private PathPreviewRenderer renderer;

    public PathPreview(ReplayModSimplePathing mod) {
        this.mod = mod;
    }

    public void register() {
        LiteModReplayMod core = mod.getCore();
        mod.getCore().getKeyBindingRegistry().registerKeyBinding("replaymod.input.pathpreview", Keyboard.KEY_H, () -> {
            SettingsRegistry registry = core.getSettingsRegistry();
            registry.set(Setting.PATH_PREVIEW, !registry.get(Setting.PATH_PREVIEW));
            registry.save();
        });
    }

    public void onReplayOpen(ReplayHandler handler) {
        replayHandler = handler;
        update();
    }

    public void onReplayClose(ReplayHandler handler) {
        replayHandler = null;
        update();
    }

    public void onSettingsChanged(SettingsRegistry.SettingKey<?> key) {
        if (key == Setting.PATH_PREVIEW) {
            update();
        }
    }

    public PathPreviewRenderer getRenderer() {
        return renderer;
    }

    private void update() {
        if (mod.getCore().getSettingsRegistry().get(Setting.PATH_PREVIEW) && replayHandler != null) {
            if (renderer == null) {
                renderer = new PathPreviewRenderer(mod, replayHandler);
            }
        } else {
            if (renderer != null) {
                renderer = null;
            }
        }
    }
}
