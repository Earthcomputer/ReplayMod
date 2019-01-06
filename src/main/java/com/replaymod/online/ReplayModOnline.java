package com.replaymod.online;

import com.replaymod.LiteModReplayMod;
import com.replaymod.online.api.ApiClient;
import com.replaymod.online.api.AuthData;
import com.replaymod.online.gui.GuiLoginPrompt;
import com.replaymod.online.gui.GuiReplayDownloading;
import com.replaymod.online.gui.GuiSaveModifiedReplay;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.replaystudio.replay.ZipReplayFile;
import com.replaymod.replaystudio.studio.ReplayStudio;
import de.johni0702.minecraft.gui.container.GuiScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

import static net.minecraft.client.Minecraft.getMinecraft;

public class ReplayModOnline {
    public static ReplayModOnline instance;

    private LiteModReplayMod core;

    private ReplayModReplay replayModule;

    public static Logger LOGGER;

    private ApiClient apiClient;

    /**
     * In case the currently opened replay gets modified, the resulting replay file is saved to this location.
     * Usually a file within the normal replays folder with a unique name.
     * When the replay is closed, the user is asked whether they want to give it a proper name.
     */
    private File currentReplayOutputFile;

    public void init() {
        LOGGER = LogManager.getLogger("replaymod-online");
        core = LiteModReplayMod.instance;
        replayModule = ReplayModReplay.instance;

        core.getSettingsRegistry().register(Setting.class);

        // TODO
        /*
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        ConfigurationAuthData authData = new ConfigurationAuthData(config);
        */
        apiClient = new ApiClient(new AuthData() {
            @Override
            public String getUserName() {
                return null;
            }

            @Override
            public String getAuthKey() {
                return null;
            }

            @Override
            public void setData(String userName, String authKey) {

            }
        });
        //authData.load(apiClient);

        if (!getDownloadsFolder().exists()){
            if (!getDownloadsFolder().mkdirs()) {
                LOGGER.warn("Failed to create downloads folder: " + getDownloadsFolder());
            }
        }
    }

    public void postInit() {
        // Initial login prompt
        if (!core.getSettingsRegistry().get(Setting.SKIP_LOGIN_PROMPT)) {
            if (!isLoggedIn()) {
                core.runLater(() -> {
                    GuiScreen parent = GuiScreen.wrap(getMinecraft().currentScreen);
                    new GuiLoginPrompt(apiClient, parent, parent, false).display();
                });
            }
        }
    }

    public LiteModReplayMod getCore() {
        return core;
    }

    public ReplayModReplay getReplayModule() {
        return replayModule;
    }

    public Logger getLogger() {
        return LOGGER;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public boolean isLoggedIn() {
        return apiClient.isLoggedIn();
    }

    public File getDownloadsFolder() {
        String path = core.getSettingsRegistry().get(Setting.DOWNLOAD_PATH);
        return new File(path.startsWith("./") ? getMinecraft().mcDataDir : null, path);
    }

    public File getDownloadedFile(int id) {
        return new File(getDownloadsFolder(), id + ".mcpr");
    }

    public boolean hasDownloaded(int id) {
        return getDownloadedFile(id).exists();
    }

    public void startReplay(int id, String name, GuiScreen onDownloadCancelled) throws IOException {
        File file = getDownloadedFile(id);
        if (file.exists()) {
            currentReplayOutputFile = new File(core.getReplayFolder(), System.currentTimeMillis() + ".mcpr");
            ReplayFile replayFile = new ZipReplayFile(new ReplayStudio(), file, currentReplayOutputFile);
            replayModule.startReplay(replayFile);
        } else {
            new GuiReplayDownloading(onDownloadCancelled, this, id, name).display();
        }
    }

    public void onReplayClosed(ReplayHandler handler) {
        if (currentReplayOutputFile != null) {
            if (currentReplayOutputFile.exists()) { // Replay was modified, ask user for new name
                new GuiSaveModifiedReplay(currentReplayOutputFile).display();
            }
            currentReplayOutputFile = null;
        }
    }
}
