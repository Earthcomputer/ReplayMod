package com.replaymod.replay.mixin;

import com.replaymod.replay.handler.ReplayGuiHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameMenu.class)
public class MixinGuiIngameMenu {

    @Inject(method = "initGui", at = @At("TAIL"))
    public void onInitGui(CallbackInfo ci) {
        ReplayGuiHandler.injectIntoIngameMenu((GuiIngameMenu) (Object) this);
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    public void onActionPerformed(GuiButton button, CallbackInfo ci) {
        ReplayGuiHandler.onIngameMenuActionPerformed((GuiIngameMenu) (Object) this, button);
    }

}
