package com.replaymod.render.mixin;

import com.replaymod.render.ducks.IRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderWorker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

@Mixin(ChunkRenderDispatcher.class)
public class MixinRenderDispatcher implements IRenderDispatcher {

    @Shadow @Final private List<ChunkRenderWorker> listThreadedWorkers;

    @Shadow @Final private PriorityBlockingQueue<ChunkCompileTaskGenerator> queueChunkUpdates;

    @Override
    public List<ChunkRenderWorker> getListThreadedWorkers() {
        return listThreadedWorkers;
    }

    @Override
    public PriorityBlockingQueue<ChunkCompileTaskGenerator> getQueueChunkUpdates() {
        return queueChunkUpdates;
    }

    @Override
    public void setQueueChunkUpdates(PriorityBlockingQueue<ChunkCompileTaskGenerator> queueChunkUpdates) {
        Field modifiersField;
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
        modifiersField.setAccessible(true);
        for (Field field : getClass().getDeclaredFields()) {
            if (field.getType() == PriorityBlockingQueue.class) {
                field.setAccessible(true);
                try {
                    modifiersField.set(field, field.getModifiers() & ~Modifier.FINAL);
                    field.set(this, queueChunkUpdates);
                    return;
                } catch (Exception ignore) {
                }
            }
        }
        throw new AssertionError();
    }
}
