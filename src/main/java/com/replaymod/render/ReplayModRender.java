package com.replaymod.render;

import com.replaymod.LiteModReplayMod;
import com.replaymod.render.utils.RenderJob;
import com.replaymod.replay.ReplayHandler;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReplayModRender {
    public static ReplayModRender instance;

    private LiteModReplayMod core;

    public static Logger LOGGER;

    // TODO: config
    //private Configuration configuration;
    private final List<RenderJob> renderQueue = new ArrayList<>();

    public LiteModReplayMod getCore() {
        return core;
    }

    public void init() {
        LOGGER = LogManager.getLogger("replaymod-render");
        core = LiteModReplayMod.instance;
        //configuration = new Configuration(event.getSuggestedConfigurationFile());

        core.getSettingsRegistry().register(Setting.class);
    }

    public void onReplayClose(ReplayHandler handler) {
        renderQueue.clear();
    }

    public File getVideoFolder() {
        String path = core.getSettingsRegistry().get(Setting.RENDER_PATH);
        File folder = new File(path.startsWith("./") ? core.getMinecraft().mcDataDir : null, path);
        try {
            FileUtils.forceMkdir(folder);
        } catch (IOException e) {
            throw new ReportedException(CrashReport.makeCrashReport(e, "Cannot create video folder."));
        }
        return folder;
    }

    /*
    public Configuration getConfiguration() {
        return configuration;
    }
    */

    public List<RenderJob> getRenderQueue() {
        return renderQueue;
    }
}
