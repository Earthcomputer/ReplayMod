package com.replaymod.core.mixins;

import com.replaymod.core.ducks.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.FutureTask;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IMinecraft {

    @Shadow @Final private Queue<FutureTask<?>> scheduledTasks;
    @Shadow @Final private List<IResourcePack> defaultResourcePacks;

    @Shadow protected abstract void resize(int width, int height);

    @Shadow @Final private Timer timer;

    @Override
    public Queue<FutureTask<?>> getScheduledTasks() {
        return scheduledTasks;
    }

    @Override
    public List<IResourcePack> getDefaultResourcePacks() {
        return defaultResourcePacks;
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

    @Override
    public void doResize(int width, int height) {
        resize(width, height);
    }
}
