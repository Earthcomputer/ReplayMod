package com.replaymod.compat;

import com.replaymod.compat.optifine.DisableFastRender;
import com.replaymod.compat.shaders.ShaderBeginRender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReplayModCompat {
    public static ReplayModCompat instance;

    public static Logger LOGGER;

    public void init() {
        LOGGER = LogManager.getLogger("replaymod-compat");
    }

    public void onRenderTickStart() {
        ShaderBeginRender.onRenderTickStart();
        DisableFastRender.onRenderBegin();
    }

    public void onRenderTickEnd() {
        DisableFastRender.onRenderEnd();
    }

}
