package com.replaymod.editor;

import com.replaymod.LiteModReplayMod;
import com.replaymod.online.Setting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReplayModEditor {
    public static ReplayModEditor instance;

    private LiteModReplayMod core;

    public static Logger LOGGER;

    public void init() {
        ReplayModEditor.LOGGER = LogManager.getLogger("replaymod-editor");
        core = LiteModReplayMod.instance;

        core.getSettingsRegistry().register(Setting.class);
    }

    public LiteModReplayMod getCore() {
        return core;
    }
}
