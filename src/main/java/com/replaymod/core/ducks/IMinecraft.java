package com.replaymod.core.ducks;

import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.Timer;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.FutureTask;

public interface IMinecraft {

    Queue<FutureTask<?>> getScheduledTasks();

    List<IResourcePack> getDefaultResourcePacks();

    void setTimer(Timer timer);

    Timer getTimer();

    void doResize(int width, int height);

    ResourcePackRepository getMcResourcePackRepository();

    boolean hasCrashed();

    void setDebugCrashKeyPressTime(long debugCrashKeyPressTime);

    long getDebugCrashKeyPressTime();

    void doUpdateDebugProfilerName(int keyCount);

}
