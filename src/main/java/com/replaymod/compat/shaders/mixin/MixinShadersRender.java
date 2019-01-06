package com.replaymod.compat.shaders.mixin;

import com.replaymod.extras.playeroverview.PlayerOverview;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "shadersmod/client/ShadersRender", remap = false)
public abstract class MixinShadersRender {

    @Inject(method = "renderHand0", at = @At("HEAD"), cancellable = true)
    private static void replayModCompat_disableRenderHand0(EntityRenderer er, float partialTicks, int renderPass, CallbackInfo ci) {
        if (!PlayerOverview.instance.onRenderHand()) {
            ci.cancel();
        }
    }

}
