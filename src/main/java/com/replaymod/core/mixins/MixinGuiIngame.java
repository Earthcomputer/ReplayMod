package com.replaymod.core.mixins;

import de.johni0702.minecraft.gui.container.AbstractGuiOverlay;
import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TODO: Forge compatibility
@Mixin(GuiIngame.class)
public class MixinGuiIngame {

    @Inject(method = "renderGameOverlay", at = @At("TAIL"))
    public void onPostRenderOverlay(float partialTicks, CallbackInfo ci) {
        AbstractGuiOverlay.onRenderOverlay(partialTicks);
    }

}
