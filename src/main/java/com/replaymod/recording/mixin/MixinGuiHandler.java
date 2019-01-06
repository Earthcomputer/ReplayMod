package com.replaymod.recording.mixin;

import com.replaymod.recording.handler.RecordingGuiHandler;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWorldSelection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GuiWorldSelection.class, GuiMultiplayer.class})
public class MixinGuiHandler extends GuiScreen {

    @Inject(method = "initGui", at = @At("TAIL"))
    public void onInitGui(CallbackInfo ci) {
        RecordingGuiHandler.onGuiInit(this);
    }

}
