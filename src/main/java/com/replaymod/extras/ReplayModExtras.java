package com.replaymod.extras;

import com.replaymod.LiteModReplayMod;
import com.replaymod.extras.advancedscreenshots.AdvancedScreenshots;
import com.replaymod.extras.playeroverview.PlayerOverview;
import com.replaymod.extras.urischeme.UriSchemeExtra;
import com.replaymod.extras.youtube.YoutubeUpload;
import com.replaymod.replay.ReplayHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReplayModExtras {
    public static ReplayModExtras instance;

    private static final List<Class<? extends Extra>> builtin = Arrays.asList(
            AdvancedScreenshots.class,
            PlayerOverview.class,
            UriSchemeExtra.class,
            YoutubeUpload.class,
            FullBrightness.class,
            HotkeyButtons.class,
            LocalizationExtra.class
    );

    private final Map<Class<? extends Extra>, Extra> instances = new HashMap<>();

    public static Logger LOGGER;

    public void init() {
        LOGGER = LogManager.getLogger("replaymod-extras");
        LiteModReplayMod.instance.getSettingsRegistry().register(Setting.class);

        for (Class<? extends Extra> cls : builtin) {
            try {
                Extra extra = cls.newInstance();
                extra.register(LiteModReplayMod.instance);
                instances.put(cls, extra);
            } catch (Throwable t) {
                LOGGER.warn("Failed to load extra " + cls.getName() + ": ", t);
            }
        }
    }

    public void beginTick() {
        instances.values().forEach(Extra::beginTick);
    }

    public void endTick() {
        instances.values().forEach(Extra::endTick);
    }

    public void preReplayOpened(ReplayHandler handler) {
        instances.values().forEach(extra -> extra.preReplayOpened(handler));
    }

    public void postReplayOpened(ReplayHandler handler) {
        instances.values().forEach(extra -> extra.postReplayOpened(handler));
    }

    public void preReplayClosed(ReplayHandler handler) {
        instances.values().forEach(extra -> extra.preReplayClosed(handler));
    }

    public void postReplayClosed(ReplayHandler handler) {
        instances.values().forEach(extra -> extra.postReplayClosed(handler));
    }

    public boolean onDispatchKeyPresses() {
        boolean result = true;
        for (Extra extra : instances.values())
            result &= extra.onDispatchKeyPresses();
        return result;
    }

    public <T extends Extra> Optional<T> get(Class<T> cls) {
        return Optional.ofNullable(instances.get(cls)).map(cls::cast);
    }
}
