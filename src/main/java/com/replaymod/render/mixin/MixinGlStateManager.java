package com.replaymod.render.mixin;

import com.replaymod.render.capturer.ODSFrameCapturer;
import com.replaymod.render.ducks.IGlStateManager;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {

    @Inject(method = "enableFog", at = @At("TAIL"))
    private static void onEnableFog(CallbackInfo ci) {
        if (ODSFrameCapturer.instance != null && ODSFrameCapturer.instance.getShaderProgram() != null)
            ODSFrameCapturer.instance.getFogEnabledVariable().set(true);
    }

    @Inject(method = "disableFog", at = @At("TAIL"))
    private static void onDisableFog(CallbackInfo ci) {
        if (ODSFrameCapturer.instance != null && ODSFrameCapturer.instance.getShaderProgram() != null)
            ODSFrameCapturer.instance.getFogEnabledVariable().set(false);
    }

    @Inject(method = "enableTexture2D", at = @At("TAIL"))
    private static void onEnableTexture2D(CallbackInfo ci) {
        if (ODSFrameCapturer.instance != null && ODSFrameCapturer.instance.getShaderProgram() != null)
            ODSFrameCapturer.instance.getTextureVariables()[IGlStateManager.getActiveTextureUnit()].set(true);
    }

    @Inject(method = "disableTexture2D", at = @At("TAIL"))
    private static void onDisableTexture2D(CallbackInfo ci) {
        if (ODSFrameCapturer.instance != null && ODSFrameCapturer.instance.getShaderProgram() != null)
            ODSFrameCapturer.instance.getTextureVariables()[IGlStateManager.getActiveTextureUnit()].set(false);
    }

}
