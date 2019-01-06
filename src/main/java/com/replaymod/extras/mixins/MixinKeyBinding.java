package com.replaymod.extras.mixins;

import com.replaymod.extras.ducks.IKeyBinding;
import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(KeyBinding.class)
public class MixinKeyBinding implements IKeyBinding {

    @Shadow private int pressTime;

    @Override
    public void setPressTime(int pressTime) {
        this.pressTime = pressTime;
    }

    @Override
    public int getPressTime() {
        return pressTime;
    }

}
