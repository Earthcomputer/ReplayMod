package com.replaymod.render.mixin;

import com.replaymod.render.ducks.IBooleanState;
import com.replaymod.render.ducks.ITextureState;
import org.spongepowered.asm.mixin.Mixin;

import java.lang.reflect.Field;

@Mixin(targets = "net/minecraft/client/renderer/GlStateManager$TextureState")
public class MixinTextureState implements ITextureState {

    @Override
    public IBooleanState getTexture2D() {
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
    public void setTexture2D(IBooleanState texture2D) {
        for (Field field : getClass().getDeclaredFields()) {
            if (IBooleanState.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    field.set(this, texture2D);
                } catch (Exception ignore) {
                }
                break;
            }
        }
    }
}
