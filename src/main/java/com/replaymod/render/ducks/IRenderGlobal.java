package com.replaymod.render.ducks;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunk;

import java.util.Set;

public interface IRenderGlobal {

    ChunkRenderDispatcher getRenderDispatcher();

    void setDisplayListEntitiesDirty(boolean displayListEntitiesDirty);

    Set<RenderChunk> getChunksToUpdate();

    void setRenderEntitiesStartupCounter(int renderEntitiesStartupCounter);

}
