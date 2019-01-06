package com.replaymod.render.hooks;

import com.replaymod.render.RenderSettings;
import com.replaymod.render.capturer.CaptureData;
import com.replaymod.render.capturer.WorldRenderer;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;

public class EntityRendererHandler implements WorldRenderer {
    public final Minecraft mc = Minecraft.getMinecraft();

    @Getter
    protected final RenderSettings settings;

    public CaptureData data;

    public boolean omnidirectional;

    public EntityRendererHandler(RenderSettings settings) {
        this.settings = settings;

        ((IEntityRenderer) mc.entityRenderer).replayModRender_setHandler(this);
    }

    @Override
    public void renderWorld(final float partialTicks, CaptureData data) {
        this.data = data;
        renderWorld(partialTicks, 0);
    }

    public void renderWorld(float partialTicks, long finishTimeNano) {
        // TODO: FML compatibility
        //FMLCommonHandler.instance().onRenderTickStart(partialTicks);

        ((com.replaymod.render.ducks.IEntityRenderer) mc.entityRenderer).doUpdateLightmap(partialTicks);

        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.5F);

        ((com.replaymod.render.ducks.IEntityRenderer) mc.entityRenderer).doRenderWorldPass(2, partialTicks, finishTimeNano);

        // TODO: FML compatibility
        //FMLCommonHandler.instance().onRenderTickEnd(partialTicks);
    }

    @Override
    public void close() throws IOException {
        ((IEntityRenderer) mc.entityRenderer).replayModRender_setHandler(null);
    }

    @Override
    public void setOmnidirectional(boolean omnidirectional) {
        this.omnidirectional = omnidirectional;
    }

    public interface GluPerspective {
        void replayModRender_gluPerspective(float fovY, float aspect, float zNear, float zFar);
    }

    public interface IEntityRenderer {
        void replayModRender_setHandler(EntityRendererHandler handler);
        EntityRendererHandler replayModRender_getHandler();
    }
}
