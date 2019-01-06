package com.replaymod.extras.mixins;

import com.replaymod.extras.ducks.IEntityRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer implements IEntityRenderer {

    @Shadow private boolean lightmapUpdateNeeded;

    @Override
    public void setLightmapUpdateNeeded(boolean lightmapUpdateNeeded) {
        this.lightmapUpdateNeeded = lightmapUpdateNeeded;
    }
}
