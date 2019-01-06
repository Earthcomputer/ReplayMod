package com.replaymod.core.ducks;

import java.util.Queue;
import java.util.concurrent.FutureTask;

public interface IMinecraft {

    Queue<FutureTask<?>> getScheduledTasks();

}
