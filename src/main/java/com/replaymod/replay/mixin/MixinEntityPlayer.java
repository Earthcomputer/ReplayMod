package com.replaymod.replay.mixin;

import com.replaymod.replay.ducks.IEntityPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayer implements IEntityPlayer {

    @Shadow private ItemStack itemStackMainHand;

    @Override
    public ItemStack getItemStackMainHand() {
        return itemStackMainHand;
    }

    @Override
    public void setItemStackMainHand(ItemStack itemStackMainHand) {
        this.itemStackMainHand = itemStackMainHand;
    }
}
