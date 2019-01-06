package com.replaymod.core.utils;

import com.replaymod.core.ducks.ITimer;
import net.minecraft.util.Timer;

public class WrappedTimer extends Timer {
    public static final float DEFAULT_MS_PER_TICK = 1000 / 20;

    protected final Timer wrapped;

    public WrappedTimer(Timer wrapped) {
        super(0);
        this.wrapped = wrapped;
        copy(wrapped, this);
    }

    @Override
    public void updateTimer() {
        copy(this, wrapped);
        wrapped.updateTimer();
        copy(wrapped, this);
    }

    protected void copy(Timer from, Timer to) {
        ITimer ifrom = (ITimer) from;
        ITimer ito = (ITimer) to;
        to.elapsedTicks = from.elapsedTicks;
        to.renderPartialTicks = from.renderPartialTicks;
        ito.setLastSyncSysClock(ifrom.getLastSyncSysClock());
        to.elapsedPartialTicks = from.elapsedPartialTicks;
        ito.setTickLength(ifrom.getTickLength());
    }
}
