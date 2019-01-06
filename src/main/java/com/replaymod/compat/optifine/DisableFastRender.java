package com.replaymod.compat.optifine;

import com.replaymod.compat.CompatUtil;
import net.minecraft.client.Minecraft;

public class DisableFastRender {

    private static boolean wasFastRender = false;

    public static void onRenderBegin() {
        if (!CompatUtil.hasOptifine()) return;

        Minecraft mc = Minecraft.getMinecraft();

        try {
            wasFastRender = (boolean) OptifineReflection.gameSettings_ofFastRender.get(mc.gameSettings);
            OptifineReflection.gameSettings_ofFastRender.set(mc.gameSettings, false);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void onRenderEnd() {
        if (!CompatUtil.hasOptifine()) return;

        Minecraft mc = Minecraft.getMinecraft();

        try {
            OptifineReflection.gameSettings_ofFastRender.set(mc.gameSettings, wasFastRender);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
