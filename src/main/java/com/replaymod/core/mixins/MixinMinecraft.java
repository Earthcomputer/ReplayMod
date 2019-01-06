package com.replaymod.core.mixins;

import com.replaymod.LiteModReplayMod;
import com.replaymod.core.ducks.IMinecraft;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;
import java.util.concurrent.FutureTask;

@Mixin(Minecraft.class)
public class MixinMinecraft implements IMinecraft {

    @Shadow @Final private Queue<FutureTask<?>> scheduledTasks;

    @Inject(method = "init", at = @At("TAIL"))
    public void onPostInit(CallbackInfo ci) {
        LiteModReplayMod.instance.postInit();
    }

    @Override
    public Queue<FutureTask<?>> getScheduledTasks() {
        return scheduledTasks;
    }
}
