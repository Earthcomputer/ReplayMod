package com.replaymod;

import com.google.common.io.Files;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mumfrey.liteloader.*;
import com.mumfrey.liteloader.core.LiteLoader;
import com.replaymod.compat.CompatUtil;
import com.replaymod.compat.ReplayModCompat;
import com.replaymod.core.KeyBindingRegistry;
import com.replaymod.core.Setting;
import com.replaymod.core.SettingsRegistry;
import com.replaymod.core.ducks.IMinecraft;
import com.replaymod.core.gui.GuiReplaySettings;
import com.replaymod.core.gui.RestoreReplayGui;
import com.replaymod.core.utils.OpenGLUtils;
import com.replaymod.editor.ReplayModEditor;
import com.replaymod.editor.handler.EditorGuiHandler;
import com.replaymod.extras.ReplayModExtras;
import com.replaymod.online.ReplayModOnline;
import com.replaymod.online.handler.OnlineGuiHandler;
import com.replaymod.recording.ReplayModRecording;
import com.replaymod.render.ReplayModRender;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.ReplaySender;
import com.replaymod.replay.camera.CameraEntity;
import com.replaymod.replay.handler.ReplayGuiHandler;
import com.replaymod.replaystudio.util.I18n;
import com.replaymod.simplepathing.ReplayModSimplePathing;
import de.johni0702.minecraft.gui.container.AbstractGuiOverlay;
import de.johni0702.minecraft.gui.container.GuiScreen;
import de.johni0702.minecraft.gui.container.VanillaGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LiteModReplayMod implements LiteMod, InitCompleteListener, HUDRenderListener, RenderListener, GameLoopListener, Tickable {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final IMinecraft imc = (IMinecraft) Minecraft.getMinecraft();

    private final KeyBindingRegistry keyBindingRegistry = new KeyBindingRegistry();
    private final SettingsRegistry settingsRegistry = new SettingsRegistry();

    public static final ResourceLocation TEXTURE = new ResourceLocation("replaymod", "replay_gui.png");
    public static final int TEXTURE_SIZE = 256;

    public static LiteModReplayMod instance;
    private static boolean hasInitialized = false;

    public static String getMinecraftVersion() {
        return "1.12";
    }

    /*
    @Deprecated
    public static Configuration config;
    */

    public LiteModReplayMod() {
        instance = this;
        ReplayModCompat.instance = new ReplayModCompat();
        ReplayModExtras.instance = new ReplayModExtras();
        ReplayModEditor.instance = new ReplayModEditor();
        ReplayModOnline.instance = new ReplayModOnline();
        ReplayModRecording.instance = new ReplayModRecording();
        ReplayModRender.instance = new ReplayModRender();
        ReplayModReplay.instance = new ReplayModReplay();
        ReplayModSimplePathing.instance = new ReplayModSimplePathing();
    }

    public KeyBindingRegistry getKeyBindingRegistry() {
        return keyBindingRegistry;
    }

    public SettingsRegistry getSettingsRegistry() {
        return settingsRegistry;
    }

    public File getReplayFolder() throws IOException {
        String path = getSettingsRegistry().get(Setting.RECORDING_PATH);
        File folder = new File(path.startsWith("./") ? getMinecraft().mcDataDir : null, path);
        FileUtils.forceMkdir(folder);
        return folder;
    }

    @Override
    public String getVersion() {
        return "@MOD_VERSION@";
    }

    @Override
    public void init(File configPath) {
        // Initialize the static OpenGL info field from the minecraft main thread
        // Unfortunately lwjgl uses static methods so we have to make use of magic init calls as well
        OpenGLUtils.init();

        I18n.setI18n(net.minecraft.client.resources.I18n::format);

        // TODO: configuration
        /*
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        settingsRegistry.setConfiguration(config);
        */

        getSettingsRegistry().register(Setting.class);

        ReplayModCompat.instance.init();
        ReplayModExtras.instance.init();
        ReplayModEditor.instance.init();
        ReplayModOnline.instance.init();
        ReplayModRecording.instance.init();
        ReplayModRender.instance.init();
        ReplayModReplay.instance.init();
        ReplayModSimplePathing.instance.init();

        hasInitialized = true;
    }

    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath) {
    }

    @Override
    public String getName() {
        return "replaymod";
    }

    @Override
    public void onInitCompleted(Minecraft minecraft, LiteLoader liteLoader) {
        getKeyBindingRegistry().registerKeyBinding("replaymod.input.settings", 0, () -> {
            new GuiReplaySettings(null, settingsRegistry).display();
        });

        settingsRegistry.save(); // Save default values to disk

        if(!CompatUtil.hasOptifine())
            GameSettings.Options.RENDER_DISTANCE.setValueMax(64f);

        runLater(() -> {
            // Cleanup deleted corrupted replays
            try {
                File[] files = getReplayFolder().listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory() && file.getName().endsWith(".mcpr.del")) {
                            if (file.lastModified() + 2 * 24 * 60 * 60 * 1000 < System.currentTimeMillis()) {
                                FileUtils.deleteDirectory(file);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Restore corrupted replays
            try {
                File[] files = getReplayFolder().listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory() && file.getName().endsWith(".mcpr.tmp")) {
                            File origFile = new File(file.getParentFile(), Files.getNameWithoutExtension(file.getName()));
                            new RestoreReplayGui(GuiScreen.wrap(mc.currentScreen), origFile).display();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        ReplayModOnline.instance.postInit();
        ReplayModReplay.instance.postInit();
    }

    /**
     * Set when the currently running code has been scheduled by runLater.
     * If this is the case, subsequent calls to runLater have to be delayed until all scheduled tasks have been
     * processed, otherwise a livelock may occur.
     */
    private boolean inRunLater = false;
    private List<Runnable> toRunLater = new ArrayList<>();

    public void runLater(Runnable runnable) {
        if (mc.isCallingFromMinecraftThread() && inRunLater) {
            toRunLater.add(runnable);
            return;
        }
        synchronized (imc.getScheduledTasks()) {
            imc.getScheduledTasks().add(ListenableFutureTask.create(() -> {
                inRunLater = true;
                try {
                    runnable.run();
                } finally {
                    inRunLater = false;
                }
            }, null));
        }
    }

    @Override
    public void onRunGameLoop(Minecraft minecraft) {
        CameraEntity.onPreClientTick();
        ReplaySender.instances.forEach(ReplaySender::onWorldTick);
        AbstractGuiOverlay.onTickOverlay();
        VanillaGuiScreen.onTickOverlay();
    }

    @Override
    public void onRender() {
        if (!hasInitialized)
            return;

        while (!toRunLater.isEmpty()) {
            Runnable task = toRunLater.remove(0);
            runLater(task);
        }
        keyBindingRegistry.onTick();
        ReplayModCompat.instance.onRenderTickStart();
        ReplayModExtras.instance.beginTick();
        if (ReplayModRecording.instance.getRecordingEventHandler() != null)
            ReplayModRecording.instance.getRecordingEventHandler().checkForGamePaused();
        CameraEntity.onRenderUpdate();
    }

    @Override
    public void onRenderGui(net.minecraft.client.gui.GuiScreen currentScreen) {
    }

    @Override
    public void onSetupCameraTransform() {
    }

    @Override
    public void onPreRenderHUD(int screenWidth, int screenHeight) {
    }

    @Override
    public void onPostRenderHUD(int screenWidth, int screenHeight) {
        ReplayModRecording.instance.getConnectionEventHandler().getGuiOverlay().renderRecordingIndicator();
    }

    public void onRenderWorldLast(int pass, float partialTicks) {
        if (ReplayModSimplePathing.instance.getPathPreview().getRenderer() != null) {
            ReplayModSimplePathing.instance.getPathPreview().getRenderer().renderCameraPath();
        }
    }

    @Override
    public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock) {
        ReplayModCompat.instance.onRenderTickEnd();
        ReplayModExtras.instance.endTick();
    }

    public void printInfoToChat(String message, Object... args) {
        printToChat(false, message, args);
    }

    public void printWarningToChat(String message, Object... args) {
        printToChat(true, message, args);
    }

    private void printToChat(boolean warning, String message, Object... args) {
        if (getSettingsRegistry().get(Setting.NOTIFICATIONS)) {
            // Some nostalgia: "§8[§6Replay Mod§8]§r Your message goes here"
            Style coloredDarkGray = new Style().setColor(TextFormatting.DARK_GRAY);
            Style coloredGold = new Style().setColor(TextFormatting.GOLD);
            ITextComponent text = new TextComponentString("[").setStyle(coloredDarkGray)
                    .appendSibling(new TextComponentTranslation("replaymod.title").setStyle(coloredGold))
                    .appendSibling(new TextComponentString("] "))
                    .appendSibling(new TextComponentTranslation(message, args).setStyle(new Style()
                            .setColor(warning ? TextFormatting.RED : TextFormatting.DARK_GREEN)));
            // Send message to chat GUI
            // The ingame GUI is initialized at startup, therefore this is possible before the client is connected
            mc.ingameGUI.getChatGUI().printChatMessage(text);
        }
    }

    public void preReplayOpened(ReplayHandler handler) {
        ReplayModExtras.instance.preReplayOpened(handler);
    }

    public void postReplayOpened(ReplayHandler handler) {
        ReplayModExtras.instance.postReplayOpened(handler);
        ReplayModSimplePathing.instance.postReplayOpen(handler);
    }

    public void preReplayClosed(ReplayHandler handler) {
        ReplayModExtras.instance.preReplayClosed(handler);
        ReplayModOnline.instance.onReplayClosed(handler);
        ReplayModRender.instance.onReplayClose(handler);
    }

    public void postReplayClosed(ReplayHandler handler) {
        ReplayModExtras.instance.postReplayClosed(handler);
        ReplayModSimplePathing.instance.onReplayClose(handler);
    }

    public boolean onDispatchKeyPresses() {
        return ReplayModExtras.instance.onDispatchKeyPresses();
    }

    public void injectIntoMainMenu(GuiMainMenu gui) {
        EditorGuiHandler.injectIntoMainMenu(gui);
        OnlineGuiHandler.injectIntoMainMenu(gui);
        ReplayGuiHandler.injectIntoMainMenu(gui);
    }

    public void onMainMenuActionPerformed(GuiMainMenu gui, GuiButton button) {
        EditorGuiHandler.onMainMenuActionPerformed(gui, button);
        OnlineGuiHandler.onMainMenuActionPerformed(gui, button);
        ReplayGuiHandler.onMainMenuActionPerformed(gui, button);
    }

    private List<Runnable> replayTimerListeners = new ArrayList<>();

    public void addReplayTimerListener(Runnable listener) {
        replayTimerListeners.add(listener);
    }

    public void removeReplayTimerListener(Runnable listener) {
        replayTimerListeners.remove(listener);
    }

    public void onReplayTimerUpdated() {
        for (int i = replayTimerListeners.size() - 1; i >= 0; i--) {
            replayTimerListeners.get(i).run();
        }
    }

    public void onUnloadWorld() {
        ReplayModRecording.instance.onDisconnected();
    }

    public void onSettingChanged(SettingsRegistry registry, SettingsRegistry.SettingKey<?> key) {
        CameraEntity.onSettingsChanged(key);
        ReplayModSimplePathing.instance.onSettingsChanged(key);
    }

    public void onKeyInput() {
        keyBindingRegistry.onKeyInput();
        if (ReplayModReplay.instance.getReplayHandler() != null)
            ReplayModReplay.instance.getReplayHandler().getOverlay().onKeyInput();
    }

    public Minecraft getMinecraft() {
        return mc;
    }

    public IMinecraft getIMinecraft() {
        return imc;
    }
}
