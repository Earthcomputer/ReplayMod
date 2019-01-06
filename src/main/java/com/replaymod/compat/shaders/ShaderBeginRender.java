package com.replaymod.compat.shaders;

import com.replaymod.core.ducks.IMinecraft;
import com.replaymod.render.hooks.EntityRendererHandler;
import net.minecraft.client.Minecraft;

import java.lang.reflect.InvocationTargetException;

public class ShaderBeginRender {

    /**
     *  Invokes Shaders#beginRender when rendering a video,
     *  as this would usually get called by EntityRenderer#renderWorld,
     *  which we're not calling during rendering.
     */
    public static void onRenderTickStart() {
        Minecraft mc = Minecraft.getMinecraft();
        if (ShaderReflection.shaders_beginRender == null) return;
        if (ShaderReflection.config_isShaders == null) return;

        try {
            // check if video is being rendered
            if (((EntityRendererHandler.IEntityRenderer) mc.entityRenderer).replayModRender_getHandler() == null)
                return;

            // check if Shaders are enabled
            if (!(boolean) (ShaderReflection.config_isShaders.invoke(null))) return;

            ShaderReflection.shaders_beginRender.invoke(null, mc, ((IMinecraft) mc).getTimer().renderPartialTicks, 0);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
