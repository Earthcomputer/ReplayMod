package com.replaymod.replay.mixin;

import com.replaymod.replay.camera.CameraEntity;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {

    @Inject(method = "renderAttackIndicator", at = @At("HEAD"), cancellable = true)
    public void onRenderAttackIndicator(float partialTicks, ScaledResolution resolution, CallbackInfo ci) {
        if (!CameraEntity.preCrosshairRender())
            ci.cancel();
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    public void onRenderHotbar(ScaledResolution resolution, float partialTicks, CallbackInfo ci) {
        if (!CameraEntity.preCrosshairRender())
            ci.cancel();
    }

    @Inject(method = "renderPlayerStats", at = @At("HEAD"), cancellable = true)
    public void onRenderPlayerStats(ScaledResolution resolution, CallbackInfo ci) {
        if (!CameraEntity.preRenderPlayerStats())
            ci.cancel();
    }

    @Inject(method = "renderExpBar", at = @At("HEAD"), cancellable = true)
    public void onRenderExpBar(ScaledResolution resolution, int x, CallbackInfo ci) {
        if (!CameraEntity.preRenderExpBar())
            ci.cancel();
    }

    @Inject(method = "renderMountHealth", at = @At("HEAD"), cancellable = true)
    public void onRenderMountHealth(ScaledResolution resolution, CallbackInfo ci) {
        if (!CameraEntity.preRenderMountHealth())
            ci.cancel();
    }

    @Inject(method = "renderHorseJumpBar", at = @At("HEAD"), cancellable = true)
    public void onRenderJumpBar(ScaledResolution resolution, int x, CallbackInfo ci) {
        if (!CameraEntity.preRenderJumpBar())
            ci.cancel();
    }

    @Inject(method = "renderPotionEffects", at = @At("HEAD"), cancellable = true)
    public void onRenderPotionIcons(ScaledResolution resolution, CallbackInfo ci) {
        if (!CameraEntity.preRenderPotionIcons())
            ci.cancel();
    }

    @Inject(method = "renderGameOverlay", at = @At("HEAD"))
    public void onPreRenderGameOverlay(float partialTicks, CallbackInfo ci) {
        CameraEntity.preRenderGameOverlay();
    }

    @Inject(method = "renderGameOverlay", at = @At("TAIL"))
    public void onPostRenderGameOverlay(float partialTicks, CallbackInfo ci) {
        CameraEntity.postRenderGameOverlay();
    }

}
