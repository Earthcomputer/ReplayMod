package com.replaymod.simplepathing;

import com.replaymod.LiteModReplayMod;
import com.replaymod.core.SettingsRegistry;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.gui.overlay.GuiReplayOverlay;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.simplepathing.SPTimeline.SPPath;
import com.replaymod.simplepathing.gui.GuiPathing;
import com.replaymod.simplepathing.preview.PathPreview;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class ReplayModSimplePathing {
    public static ReplayModSimplePathing instance;

    private LiteModReplayMod core;

    public static Logger LOGGER;

    private GuiPathing guiPathing;

    private PathPreview pathPreview;

    public void init() {
        LOGGER = LogManager.getLogger("replaymod-simplepathing");
        core = LiteModReplayMod.instance;

        core.getSettingsRegistry().register(Setting.class);

        pathPreview = new PathPreview(this);
        pathPreview.register();

        core.getKeyBindingRegistry().registerKeyBinding("replaymod.input.keyframerepository", Keyboard.KEY_X, () -> {
            if (guiPathing != null) guiPathing.keyframeRepoButtonPressed();
        });
        core.getKeyBindingRegistry().registerKeyBinding("replaymod.input.clearkeyframes", Keyboard.KEY_C, () -> {
            if (guiPathing != null) guiPathing.clearKeyframesButtonPressed();
        });
        core.getKeyBindingRegistry().registerRepeatedKeyBinding("replaymod.input.synctimeline", Keyboard.KEY_V, () -> {
            if (guiPathing != null) guiPathing.syncTimeButtonPressed();
        });
        core.getKeyBindingRegistry().registerRaw(Keyboard.KEY_DELETE, () -> {
            if (guiPathing != null) guiPathing.deleteButtonPressed();
        });
    }

    public void postReplayOpen(ReplayHandler handler) {
        clearCurrentTimeline();
        guiPathing = new GuiPathing(core, this, handler);
        pathPreview.onReplayOpen(handler);
    }

    public void onReplayClose(ReplayHandler handler) {
        currentTimeline = null;
        guiPathing = null;
        selectedPath = null;
        pathPreview.onReplayClose(handler);
    }

    public void onSettingsChanged(SettingsRegistry.SettingKey<?> key) {
        if (key == Setting.DEFAULT_INTERPOLATION) {
            if (currentTimeline != null && guiPathing != null) {
                updateDefaultInterpolatorType();
            }
        }
        pathPreview.onSettingsChanged(key);
    }

    public PathPreview getPathPreview() {
        return pathPreview;
    }

    private GuiReplayOverlay getReplayOverlay() {
        return ReplayModReplay.instance.getReplayHandler().getOverlay();
    }

    private SPTimeline currentTimeline;

    private SPPath selectedPath;
    @Getter
    private long selectedTime;

    public SPPath getSelectedPath() {
        if (getReplayOverlay().timeline.getSelectedMarker() != null) {
            selectedPath = null;
            selectedTime = 0;
        }
        return selectedPath;
    }

    public boolean isSelected(Keyframe keyframe) {
        return getSelectedPath() != null && currentTimeline.getKeyframe(selectedPath, selectedTime) == keyframe;
    }

    public void setSelected(SPPath path, long time) {
        selectedPath = path;
        selectedTime = time;
        if (selectedPath != null) {
            getReplayOverlay().timeline.setSelectedMarker(null);
        }
    }

    public void setCurrentTimeline(SPTimeline newTimeline) {
        selectedPath = null;
        currentTimeline = newTimeline;
        updateDefaultInterpolatorType();
    }

    public void clearCurrentTimeline() {
        setCurrentTimeline(new SPTimeline());
    }

    public SPTimeline getCurrentTimeline() {
        return currentTimeline;
    }

    private void updateDefaultInterpolatorType() {
        InterpolatorType newDefaultType =
                InterpolatorType.fromString(core.getSettingsRegistry().get(Setting.DEFAULT_INTERPOLATION));
        currentTimeline.setDefaultInterpolatorType(newDefaultType);
    }

    public LiteModReplayMod getCore() {
        return core;
    }

    public GuiPathing getGuiPathing() {
        return guiPathing;
    }
}
