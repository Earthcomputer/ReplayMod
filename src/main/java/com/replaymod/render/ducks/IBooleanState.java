package com.replaymod.render.ducks;

import net.minecraft.client.renderer.GlStateManager;

import java.lang.reflect.Constructor;

public interface IBooleanState {

    boolean getCurrentState();

    int getCapability();

    static IBooleanState create(int capability) {
        Class<?> booleanState = null;
        for (Class<?> innerClass : GlStateManager.class.getClasses()) {
            if (IBooleanState.class.isAssignableFrom(innerClass)) {
                booleanState = innerClass;
                break;
            }
        }
        assert booleanState != null;
        try {
            Constructor<?> ctor = booleanState.getConstructor(int.class);
            ctor.setAccessible(true);
            return (IBooleanState) ctor.newInstance(capability);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

}
