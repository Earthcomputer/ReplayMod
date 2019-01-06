package com.replaymod.compat.oranges17animations;

import com.replaymod.compat.CompatUtil;
import com.replaymod.replay.camera.CameraEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;

/**
 * Orange seems to have copied vast parts of the RendererLivingEntity into their ArmorAnimation class which cancels the RenderLivingEvent.Pre and calls its own code instead.
 * This breaks our mixin which assures that, even though the camera is in spectator mode, it cannot see invisible entities.
 *
 * To fix this issue, we simply cancel the RenderLivingEvent.Pre before it gets to ArmorAnimation if the entity is invisible.
 */
public class HideInvisibleEntities {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final boolean modLoaded = CompatUtil.isForgeModLoaded("animations");

    public static boolean preRenderLiving(EntityLivingBase entity) {
        if (modLoaded) {
            if (mc.player instanceof CameraEntity && entity.isInvisible()) {
                return false;
            }
        }
        return true;
    }
}
