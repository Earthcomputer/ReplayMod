package com.replaymod.extras.playeroverview.mixin;

import com.replaymod.extras.playeroverview.PlayerOverview;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Redirect(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;isPlayerSleeping()Z"))
    public boolean isPlayerSleeping(EntityLivingBase entity) {
        return entity.isPlayerSleeping() || !PlayerOverview.instance.onRenderHand();
    }

}
