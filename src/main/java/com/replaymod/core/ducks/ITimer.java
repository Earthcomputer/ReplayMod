package com.replaymod.core.ducks;

public interface ITimer {

    void setLastSyncSysClock(long lastSyncSysClock);

    long getLastSyncSysClock();

    void setTickLength(float tickLength);

    float getTickLength();

}
