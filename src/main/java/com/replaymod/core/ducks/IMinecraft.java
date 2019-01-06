package com.replaymod.core.ducks;

import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.Timer;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.FutureTask;

public interface IMinecraft {

    Queue<FutureTask<?>> getScheduledTasks();

    List<IResourcePack> getDefaultResourcePacks();

    Timer getTimer();

    void doResize(int width, int height);

}
