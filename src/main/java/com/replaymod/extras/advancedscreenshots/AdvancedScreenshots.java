package com.replaymod.extras.advancedscreenshots;

import com.replaymod.LiteModReplayMod;
import com.replaymod.extras.Extra;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import org.lwjgl.input.Keyboard;

public class AdvancedScreenshots implements Extra {

    private LiteModReplayMod mod;

    private final Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void register(LiteModReplayMod mod) throws Exception {
        this.mod = mod;
    }

    @Override
    public boolean onDispatchKeyPresses() {
        int keyCode = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() : Keyboard.getEventKey();

        // all the conditions required to trigger a screenshot condensed in a single if statement
        if (keyCode != 0 && !Keyboard.isRepeatEvent()
                && (!(mc.currentScreen instanceof GuiControls) || ((GuiControls) mc.currentScreen).time <= mc.getSystemTime() - 20L)
                && Keyboard.getEventKeyState()
                && keyCode == mc.gameSettings.keyBindScreenshot.getKeyCode()) {

            LiteModReplayMod.instance.runLater(() -> {
                new GuiCreateScreenshot(mod).display();
            });

            return false;
        }
        return true;
    }
}
