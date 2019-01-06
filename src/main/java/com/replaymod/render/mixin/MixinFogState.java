package com.replaymod.render.mixin;

import com.replaymod.render.ducks.IBooleanState;
import com.replaymod.render.ducks.IFogState;
import org.spongepowered.asm.mixin.Mixin;

import java.lang.reflect.Field;

@Mixin(targets = "net/minecraft/client/renderer/GlStateManager$FogState")
public class MixinFogState implements IFogState {

    @Override
    public IBooleanState getFog() {
        for (Field field : getClass().getDeclaredFields()) {
            if (IBooleanState.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    return (IBooleanState) field.get(this);
                } catch (Exception ignore) {
                }
            }
        }
        throw new AssertionError();
    }

    @Override
    public void setFog(IBooleanState fog) {
        for (Field field : getClass().getDeclaredFields()) {
            if (IBooleanState.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    field.set(this, fog);
                } catch (Exception ignore) {
                }
                break;
            }
        }
    }
}
