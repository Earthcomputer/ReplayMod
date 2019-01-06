package com.replaymod.compat.oranges17animations.mixin;

import com.replaymod.compat.oranges17animations.HideInvisibleEntities;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderLivingBase.class)
public class MixinRenderLivingBase {

    @Inject(method = "doRender", at = @At("HEAD"), cancellable =  true)
    public void onDoRender(EntityLivingBase entity, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        if (!HideInvisibleEntities.preRenderLiving(entity))
            ci.cancel();
    }

}
