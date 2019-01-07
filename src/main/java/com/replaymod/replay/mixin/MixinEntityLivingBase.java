package com.replaymod.replay.mixin;

import com.replaymod.replay.ducks.IEntityLivingBase;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityLivingBase.class)
public class MixinEntityLivingBase implements IEntityLivingBase {

    @Shadow protected int activeItemStackUseCount;

    @Override
    public void setItemInUseCount(int itemInUseCount) {
        this.activeItemStackUseCount = itemInUseCount;
    }
}
