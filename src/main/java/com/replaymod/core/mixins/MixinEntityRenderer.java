package com.replaymod.core.mixins;

import com.replaymod.LiteModReplayMod;
import de.johni0702.minecraft.gui.container.VanillaGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

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

    @Inject(method = "updateCameraAndRender", at = @At("TAIL"))
    public void onDrawScreen(float partialTicks, long nanoTime, CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        final ScaledResolution resolution = new ScaledResolution(mc);
        int resWidth = resolution.getScaledWidth();
        int resHeight = resolution.getScaledHeight();
        int mouseX = Mouse.getX() * resWidth / mc.displayWidth;
        int mouseY = Mouse.getY() * resHeight / mc.displayHeight - 1;
        VanillaGuiScreen.onGuiRender(mouseX, mouseY, partialTicks);
    }

}
