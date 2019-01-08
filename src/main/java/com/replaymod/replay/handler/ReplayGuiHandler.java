package com.replaymod.replay.handler;

import com.replaymod.core.ducks.IGuiScreen;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.gui.screen.GuiReplayViewer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.replaymod.replay.ReplayModReplay.LOGGER;

public class ReplayGuiHandler {

    private static final int BUTTON_EXIT_SERVER = 1;
    private static final int BUTTON_ADVANCEMENTS = 5;
    private static final int BUTTON_STATS = 6;
    private static final int BUTTON_OPEN_TO_LAN = 7;

    private static final int BUTTON_REPLAY_VIEWER = 17890234;
    private static final int BUTTON_EXIT_REPLAY = 17890235;

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void injectIntoIngameMenu(GuiIngameMenu gui) {
        ReplayModReplay mod = ReplayModReplay.instance;

        if (mod.getReplayHandler() != null) {
            // Pause replay when menu is opened
            mod.getReplayHandler().getReplaySender().setReplaySpeed(0);

            GuiButton achievements = null, stats = null, openToLan = null;
            List<GuiButton> buttonList = ((IGuiScreen) gui).getButtonList();
            for(GuiButton b : new ArrayList<>(buttonList)) {
                switch (b.id) {
                    // Replace "Exit Server" button with "Exit Replay" button
                    case BUTTON_EXIT_SERVER:
                        b.displayString = I18n.format("replaymod.gui.exit");
                        b.id = BUTTON_EXIT_REPLAY;
                        break;
                    // Remove "Advancements", "Stats" and "Open to LAN" buttons
                    case BUTTON_ADVANCEMENTS:
                        buttonList.remove(achievements = b);
                        break;
                    case BUTTON_STATS:
                        buttonList.remove(stats = b);
                        break;
                    case BUTTON_OPEN_TO_LAN:
                        buttonList.remove(openToLan = b);
                        break;
                }
            }
            if (achievements != null && stats != null) {
                moveAllButtonsDirectlyBelowUpwards(buttonList, achievements.y,
                        achievements.x, stats.x + stats.getButtonWidth());
            }
            if (openToLan != null) {
                moveAllButtonsDirectlyBelowUpwards(buttonList, openToLan.y,
                        openToLan.x, openToLan.x + openToLan.getButtonWidth());
            }
        }
    }

    /**
     * Moves all buttons that are within a rectangle below a certain y coordinate upwards by 24 units.
     * @param buttons List of buttons
     * @param belowY The Y limit
     * @param xStart Left x limit of the rectangle
     * @param xEnd Right x limit of the rectangle
     */
    private static void moveAllButtonsDirectlyBelowUpwards(List<GuiButton> buttons, int belowY, int xStart, int xEnd) {
        for (GuiButton button : buttons) {
            if (button.y >= belowY && button.x <= xEnd && button.x + button.getButtonWidth() >= xStart) {
                button.y -= 24;
            }
        }
    }

    public static void injectIntoMainMenu(GuiMainMenu gui) {
        ReplayModReplay mod = ReplayModReplay.instance;

        if (mod.getReplayHandler() != null) {
            // Something went terribly wrong and we ended up in the main menu with the replay still active.
            // To prevent players from joining live servers and using the CameraEntity, try to stop the replay now.
            try {
                mod.getReplayHandler().endReplay();
            } catch (IOException e) {
                LOGGER.error("Trying to stop broken replay: ", e);
            } finally {
                if (mod.getReplayHandler() != null) {
                    mod.forcefullyStopReplay();
                }
            }
        }

        GuiButton button = new GuiButton(BUTTON_REPLAY_VIEWER, gui.width / 2 - 100,
                gui.height / 4 + 10 + 3 * 24, I18n.format("replaymod.gui.replayviewer"));
        button.setWidth(button.getButtonWidth() / 2 - 2);
        ((IGuiScreen) gui).getButtonList().add(button);
    }

    public static void onIngameMenuActionPerformed(GuiIngameMenu gui, GuiButton button) {
        if (button.enabled && button.id == BUTTON_EXIT_REPLAY && ReplayModReplay.instance.getReplayHandler() != null) {
            button.enabled = false;
            mc.displayGuiScreen(new GuiMainMenu());
            try {
                if (ReplayModReplay.instance.getReplayHandler() != null)
                    ReplayModReplay.instance.getReplayHandler().endReplay();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void onMainMenuActionPerformed(GuiMainMenu gui, GuiButton button) {
        if (button.enabled && button.id == BUTTON_REPLAY_VIEWER) {
            new GuiReplayViewer(ReplayModReplay.instance).display();
        }
    }
}
