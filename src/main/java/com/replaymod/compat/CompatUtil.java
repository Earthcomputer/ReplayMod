package com.replaymod.compat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CompatUtil {

    private static Method LOADER_ISMODLOADED;

    private static boolean hasOptifine;
    static {
        try {
            Class.forName("optifine.Utils");
            hasOptifine = true;
        } catch (ClassNotFoundException e) {
            hasOptifine = false;
        }

        Class<?> loaderCls;
        try {
            loaderCls = Class.forName("net.minecraftforge.fml.common.Loader");
        } catch (ClassNotFoundException e) {
            loaderCls = null; // no forge
        }
        if (loaderCls != null) {
            try {
                LOADER_ISMODLOADED = loaderCls.getMethod("isModLoaded", String.class);
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
        } else {
            LOADER_ISMODLOADED = null;
        }
    }

    public static boolean hasOptifine() {
        return hasOptifine;
    }

    public static boolean isForgeModLoaded(String modid) {
        if (LOADER_ISMODLOADED == null) {
            return false;
        } else {
            try {
                return (Boolean) LOADER_ISMODLOADED.invoke(null, modid);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new AssertionError(e);
            }
        }
    }

}
