package com.replaymod.editor.handler;

import com.replaymod.LiteModReplayMod;
import com.replaymod.core.ducks.IGuiScreen;
import com.replaymod.editor.gui.GuiReplayEditor;
import de.johni0702.minecraft.gui.container.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.resources.I18n;

public class EditorGuiHandler {
    private static final int BUTTON_REPLAY_EDITOR = 17890237;

    public static void injectIntoMainMenu(GuiMainMenu gui) {
        GuiButton button = new GuiButton(BUTTON_REPLAY_EDITOR, gui.width / 2 + 2,
                gui.height / 4 + 10 + 3 * 24, I18n.format("replaymod.gui.replayeditor"));
        button.setWidth(button.getButtonWidth() / 2 - 2);
        ((IGuiScreen) gui).getButtonList().add(button);
    }

    public static void onMainMenuActionPerformed(GuiMainMenu gui, GuiButton button) {
        if(!button.enabled) return;

        if (button.id == BUTTON_REPLAY_EDITOR) {
            new GuiReplayEditor(GuiScreen.wrap(gui), LiteModReplayMod.instance).display();
        }
    }
}
