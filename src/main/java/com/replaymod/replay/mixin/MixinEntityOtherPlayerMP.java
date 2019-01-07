package com.replaymod.replay.mixin;

import com.replaymod.replay.ducks.IEntityOtherPlayerMP;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityOtherPlayerMP.class)
public class MixinEntityOtherPlayerMP implements IEntityOtherPlayerMP {
    @Shadow private double otherPlayerMPX;

    @Shadow private double otherPlayerMPY;

    @Shadow private double otherPlayerMPZ;

    @Shadow private double otherPlayerMPYaw;

    @Shadow private double otherPlayerMPPitch;

    @Override
    public double getOtherPlayerMPX() {
        return otherPlayerMPX;
    }

    @Override
    public double getOtherPlayerMPY() {
        return otherPlayerMPY;
    }

    @Override
    public double getOtherPlayerMPZ() {
        return otherPlayerMPZ;
    }

    @Override
    public double getOtherPlayerMPYaw() {
        return otherPlayerMPYaw;
    }

    @Override
    public double getOtherPlayerMPPitch() {
        return otherPlayerMPPitch;
    }
}
