package com.replaymod.extras.youtube;

import com.replaymod.LiteModReplayMod;
import com.replaymod.extras.Extra;
import com.replaymod.render.gui.GuiRenderingDone;
import de.johni0702.minecraft.gui.element.GuiButton;

public class YoutubeUpload implements Extra {

    public static YoutubeUpload instance;
    public YoutubeUpload() {
        instance = this;
    }

    @Override
    public void register(LiteModReplayMod mod) throws Exception {
    }

    public void onRenderingDoneScreenOpened(GuiRenderingDone gui) {
        // Check if there already is a youtube button
        if (gui.actionsPanel.getChildren().stream().anyMatch(it -> it instanceof YoutubeButton)) {
            return; // Button already added
        }
        // Add the Upload to YouTube button to actions panel
        gui.actionsPanel.addElements(null,
                new YoutubeButton().onClick(() ->
                        new GuiYoutubeUpload(gui, gui.videoFile, gui.videoFrames, gui.settings).display()
                ).setSize(200, 20).setI18nLabel("replaymod.gui.youtubeupload"));
    }

    private static class YoutubeButton extends GuiButton {}
}
