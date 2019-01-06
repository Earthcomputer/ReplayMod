package com.replaymod.recording.handler;

import com.replaymod.LiteModReplayMod;
import com.replaymod.core.SettingsRegistry;
import com.replaymod.recording.Setting;
import de.johni0702.minecraft.gui.container.GuiScreen;
import de.johni0702.minecraft.gui.container.VanillaGuiScreen;
import de.johni0702.minecraft.gui.element.GuiCheckbox;
import de.johni0702.minecraft.gui.layout.CustomLayout;
import net.minecraft.client.gui.GuiWorldSelection;

public class RecordingGuiHandler {

    public static void onGuiInit(net.minecraft.client.gui.GuiScreen gui) {
        LiteModReplayMod mod = LiteModReplayMod.instance;
        SettingsRegistry settingsRegistry = mod.getSettingsRegistry();
        boolean sp = gui instanceof GuiWorldSelection;
        Setting<Boolean> setting = sp ? Setting.RECORD_SINGLEPLAYER : Setting.RECORD_SERVER;

        GuiCheckbox recordingCheckbox = new GuiCheckbox()
                .setI18nLabel("replaymod.gui.settings.record" + (sp ? "singleplayer" : "server"))
                .setChecked(settingsRegistry.get(setting));
        recordingCheckbox.onClick(() -> {
            settingsRegistry.set(setting, recordingCheckbox.isChecked());
            settingsRegistry.save();
        });

        VanillaGuiScreen.setup(gui).setLayout(new CustomLayout<GuiScreen>() {
            @Override
            protected void layout(GuiScreen container, int width, int height) {
                //size(recordingCheckbox, 200, 20);
                pos(recordingCheckbox, width - width(recordingCheckbox) - 5, 5);
            }
        }).addElements(null, recordingCheckbox);
    }
}
