package com.replaymod;

import com.google.common.io.Files;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mumfrey.liteloader.LiteMod;
import com.mumfrey.liteloader.Tickable;
import com.replaymod.core.KeyBindingRegistry;
import com.replaymod.core.Setting;
import com.replaymod.core.SettingsRegistry;
import com.replaymod.core.ducks.IMinecraft;
import com.replaymod.core.gui.GuiReplaySettings;
import com.replaymod.core.gui.RestoreReplayGui;
import com.replaymod.core.utils.OpenGLUtils;
import com.replaymod.replaystudio.util.I18n;
import de.johni0702.minecraft.gui.container.GuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LiteModReplayMod implements LiteMod, Tickable {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final IMinecraft imc = (IMinecraft) Minecraft.getMinecraft();

    private final KeyBindingRegistry keyBindingRegistry = new KeyBindingRegistry();
    private final SettingsRegistry settingsRegistry = new SettingsRegistry();

    public static final ResourceLocation TEXTURE = new ResourceLocation("replaymod", "replay_gui.png");
    public static final int TEXTURE_SIZE = 256;

    public static LiteModReplayMod instance;

    /*
    @Deprecated
    public static Configuration config;
    */

    public LiteModReplayMod() {
        instance = this;
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
    }

    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath) {
    }

    @Override
    public String getName() {
        return "replaymod";
    }

    public void postInit() {
        getKeyBindingRegistry().registerKeyBinding("replaymod.input.settings", 0, () -> {
            new GuiReplaySettings(null, settingsRegistry).display();
        });

        settingsRegistry.save(); // Save default values to disk

        boolean hasOptifine;
        try {
            Class.forName("optifine.Utils");
            hasOptifine = true;
        } catch (ClassNotFoundException e) {
            hasOptifine = false;
        }

        if(!hasOptifine)
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
    public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock) {
        if (clock) {
            keyBindingRegistry.onKeyInput();
        } else {
            while (!toRunLater.isEmpty()) {
                Runnable task = toRunLater.remove(0);
                runLater(task);
            }
            keyBindingRegistry.onTick();
        }
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

    public Minecraft getMinecraft() {
        return mc;
    }

    public IMinecraft getIMinecraft() {
        return imc;
    }
}
