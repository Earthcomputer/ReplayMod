package com.replaymod.online.handler;

import com.replaymod.core.ducks.IGuiScreen;
import com.replaymod.online.ReplayModOnline;
import com.replaymod.online.gui.GuiLoginPrompt;
import com.replaymod.online.gui.GuiReplayCenter;
import com.replaymod.online.gui.GuiUploadReplay;
import com.replaymod.replay.gui.screen.GuiReplayViewer;
import de.johni0702.minecraft.gui.container.AbstractGuiScreen;
import de.johni0702.minecraft.gui.container.GuiPanel;
import de.johni0702.minecraft.gui.container.GuiScreen;
import de.johni0702.minecraft.gui.element.GuiElement;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.resources.I18n;

import java.io.File;

public class OnlineGuiHandler {
    private static final int BUTTON_REPLAY_CENTER = 17890236;

    private static final ReplayModOnline mod = ReplayModOnline.instance;

    public static void injectIntoMainMenu(GuiMainMenu gui) {
        GuiButton button = new GuiButton(BUTTON_REPLAY_CENTER, gui.width / 2 - 100,
                gui.height / 4 + 10 + 4 * 24, I18n.format("replaymod.gui.replaycenter"));
        ((IGuiScreen) gui).getButtonList().add(button);
    }

    public static void injectIntoReplayViewer(GuiReplayViewer gui) {
        AbstractGuiScreen guiScreen = gui;
        final GuiReplayViewer replayViewer = (GuiReplayViewer) guiScreen;
        // Inject Upload button
        for (GuiElement element : replayViewer.replayButtonPanel.getChildren()) {
            if (element instanceof GuiPanel && (((GuiPanel) element).getChildren().isEmpty())) {
                new de.johni0702.minecraft.gui.element.GuiButton((GuiPanel) element).onClick(new Runnable() {
                    @Override
                    public void run() {
                        File replayFile = replayViewer.list.getSelected().file;
                        GuiUploadReplay uploadGui = new GuiUploadReplay(replayViewer, mod, replayFile);
                        if (mod.isLoggedIn()) {
                            uploadGui.display();
                        } else {
                            new GuiLoginPrompt(mod.getApiClient(), replayViewer, uploadGui, true);
                        }
                    }
                }).setSize(73, 20).setI18nLabel("replaymod.gui.upload").setDisabled();
            }
        }
    }

    public static void onMainMenuActionPerformed(GuiMainMenu gui, GuiButton button) {
        if(!button.enabled) return;

        if (button.id == BUTTON_REPLAY_CENTER) {
            GuiReplayCenter replayCenter = new GuiReplayCenter(mod);
            if (mod.isLoggedIn()) {
                replayCenter.display();
            } else {
                new GuiLoginPrompt(mod.getApiClient(), GuiScreen.wrap(gui), replayCenter, true).display();
            }
        }
    }
}
