package com.replaymod.render.ducks;

import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkRenderWorker;

import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

public interface IRenderDispatcher {

    List<ChunkRenderWorker> getListThreadedWorkers();

    PriorityBlockingQueue<ChunkCompileTaskGenerator> getQueueChunkUpdates();

    void setQueueChunkUpdates(PriorityBlockingQueue<ChunkCompileTaskGenerator> queueChunkUpdates);

}
