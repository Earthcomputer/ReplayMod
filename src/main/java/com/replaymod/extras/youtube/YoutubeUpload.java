package com.replaymod.extras.youtube;

import com.replaymod.extras.Extra;
import com.replaymod.render.gui.GuiRenderingDone;
import de.johni0702.minecraft.gui.container.GuiScreen;
import de.johni0702.minecraft.gui.element.GuiButton;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class YoutubeUpload implements Extra {
    @Override
    public void register(ReplayMod mod) throws Exception {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onGuiOpen(GuiScreenEvent.InitGuiEvent.Post event) {
        if (GuiScreen.from(event.getGui()) instanceof GuiRenderingDone) {
            GuiRenderingDone gui = (GuiRenderingDone) GuiScreen.from(event.getGui());
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
    }

    private static class YoutubeButton extends GuiButton {}
}
