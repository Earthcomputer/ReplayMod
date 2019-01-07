package com.replaymod.replay.mixin;

import com.replaymod.replay.camera.CameraEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Inject(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;debugCamEnable:Z", ordinal = 3))
    public void onOrientCamera(float partialTicks, CallbackInfo ci) {
        if (!Minecraft.getMinecraft().gameSettings.debugCamEnable) {
            CameraEntity.onEntityViewRenderEvent();
        }
    }

}
