package com.replaymod.replay.ducks;

import net.minecraft.item.ItemStack;

public interface IItemRenderer {

    void setPrevEquippedProgressMainHand(float prevEquippedProgressMainHand);

    void setPrevEquippedProgressOffHand(float prevEquippedProgressOffHand);

    void setEquippedProgressMainHand(float equippedProgressMainHand);

    void setEquippedProgressOffHand(float equippedProgressOffHand);

    void setItemStackMainHand(ItemStack itemStackMainHand);

    void setItemStackOffHand(ItemStack itemStackOffHand);

}
