package com.replaymod.replay.mixin;

import com.replaymod.replay.ducks.IItemRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer implements IItemRenderer {

    @Shadow private float prevEquippedProgressMainHand;

    @Shadow private float prevEquippedProgressOffHand;

    @Shadow private float equippedProgressMainHand;

    @Shadow private float equippedProgressOffHand;

    @Shadow private ItemStack itemStackMainHand;

    @Shadow private ItemStack itemStackOffHand;

    @Override
    public void setPrevEquippedProgressMainHand(float prevEquippedProgressMainHand) {
        this.prevEquippedProgressMainHand = prevEquippedProgressMainHand;
    }

    @Override
    public void setPrevEquippedProgressOffHand(float prevEquippedProgressOffHand) {
        this.prevEquippedProgressOffHand = prevEquippedProgressOffHand;
    }

    @Override
    public void setEquippedProgressMainHand(float equippedProgressMainHand) {
        this.equippedProgressMainHand = equippedProgressMainHand;
    }

    @Override
    public void setEquippedProgressOffHand(float equippedProgressOffHand) {
        this.equippedProgressOffHand = equippedProgressOffHand;
    }

    @Override
    public void setItemStackMainHand(ItemStack itemStackMainHand) {
        this.itemStackMainHand = itemStackMainHand;
    }

    @Override
    public void setItemStackOffHand(ItemStack itemStackOffHand) {
        this.itemStackOffHand = itemStackOffHand;
    }
}
