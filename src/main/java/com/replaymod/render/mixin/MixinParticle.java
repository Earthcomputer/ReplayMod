package com.replaymod.render.mixin;

import com.replaymod.render.ducks.IParticle;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Particle.class)
public class MixinParticle implements IParticle {

    @Shadow protected double prevPosX;

    @Shadow protected double prevPosY;

    @Shadow protected double prevPosZ;

    @Shadow protected double posX;

    @Shadow protected double posY;

    @Shadow protected double posZ;

    @Override
    public double getPrevPosX() {
        return prevPosX;
    }

    @Override
    public double getPrevPosY() {
        return prevPosY;
    }

    @Override
    public double getPrevPosZ() {
        return prevPosZ;
    }

    @Override
    public double getPosX() {
        return posX;
    }

    @Override
    public double getPosY() {
        return posY;
    }

    @Override
    public double getPosZ() {
        return posZ;
    }
}
