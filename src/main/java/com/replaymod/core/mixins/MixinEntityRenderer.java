package com.replaymod.core.mixins;

import com.replaymod.LiteModReplayMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Inject(method = "renderWorldPass", at = @At(
            value = "INVOKE_STRING",
            target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
            args = {"ldc=hand"}))
    public void onRenderWorldLast(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        Minecraft.getMinecraft().mcProfiler.endStartSection("replay_render_last");
        LiteModReplayMod.instance.onRenderWorldLast(pass, partialTicks);
    }

}
