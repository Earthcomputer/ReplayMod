package com.replaymod.render.mixin;

import com.replaymod.render.ducks.IBooleanState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/client/renderer/GlStateManager$BooleanState")
public class MixinBooleanState implements IBooleanState {

    @Shadow @Final private int capability;
    @Shadow private boolean currentState;

    @Override
    public boolean getCurrentState() {
        return currentState;
    }

    @Override
    public int getCapability() {
        return capability;
    }
}
