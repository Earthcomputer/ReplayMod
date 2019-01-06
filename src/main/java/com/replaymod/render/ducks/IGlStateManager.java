package com.replaymod.render.ducks;

import net.minecraft.client.renderer.GlStateManager;

import java.lang.reflect.Field;

public class IGlStateManager {

    public static IFogState getFogState() {
        for (Field field : GlStateManager.class.getDeclaredFields()) {
            if (IFogState.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    return (IFogState) field.get(null);
                } catch (Exception ignore) {
                }
            }
        }
        throw new AssertionError();
    }

    public static ITextureState[] getTextureStates() {
        for (Field field : GlStateManager.class.getDeclaredFields()) {
            if (field.getType().isArray() && ITextureState.class.isAssignableFrom(field.getType().getComponentType())) {
                field.setAccessible(true);
                try {
                    return (ITextureState[]) field.get(null);
                } catch (Exception ignore) {
                }
            }
        }
        throw new AssertionError();
    }

    public static int getActiveTextureUnit() {
        for (Field field : GlStateManager.class.getDeclaredFields()) {
            if (field.getType() == int.class) {
                field.setAccessible(true);
                try {
                    return field.getInt(null);
                } catch (Exception ignore) {
                }
            }
        }
        throw new AssertionError();
    }

}
