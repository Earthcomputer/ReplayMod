package com.replaymod.recording.mixin;

import com.replaymod.recording.ReplayModRecording;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayer {

    @Inject(method = "onUpdate", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        ReplayModRecording.instance.getRecordingEventHandler().onPlayerTick((EntityPlayer) (Object) this);
    }

    @Inject(method = "trySleep", at = @At("HEAD"))
    public void onSleep(BlockPos pos, CallbackInfoReturnable<EntityPlayer.SleepResult> ci) {
        ReplayModRecording.instance.getRecordingEventHandler().onSleep((EntityPlayer) (Object) this, pos);
    }

    @Inject(method = "interactOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;processInitialInteract(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;)Z"))
    public void onInteract(Entity entity, EnumHand hand, CallbackInfoReturnable<EnumActionResult> ci) {
        if (entity instanceof EntityMinecart) {
            ReplayModRecording.instance.getRecordingEventHandler().enterMinecart((EntityPlayer) (Object) this, (EntityMinecart) entity);
        }
    }

}
