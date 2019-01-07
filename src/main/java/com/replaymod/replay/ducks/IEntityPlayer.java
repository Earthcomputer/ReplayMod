package com.replaymod.replay.ducks;

import net.minecraft.item.ItemStack;

public interface IEntityPlayer {

    ItemStack getItemStackMainHand();

    void setItemStackMainHand(ItemStack itemStackMainHand);

}
