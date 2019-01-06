package com.replaymod.render.ducks;

public interface IEntityRenderer {

    void doUpdateLightmap(float partialTicks);

    void doRenderWorldPass(int pass, float partialTicks, long finishTimeNano);

}
