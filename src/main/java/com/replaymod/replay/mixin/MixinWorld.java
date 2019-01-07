package com.replaymod.replay.mixin;

import com.replaymod.replay.ducks.IWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(World.class)
public abstract class MixinWorld implements IWorld {

    @Shadow protected abstract void onEntityRemoved(Entity entityIn);

    @Override
    public void doOnEntityRemoved(Entity entity) {
        onEntityRemoved(entity);
    }
}
