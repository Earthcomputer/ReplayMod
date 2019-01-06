package com.replaymod.core.mixins;

import com.replaymod.core.ducks.ITimer;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Timer.class)
public class MixinTimer implements ITimer {

    @Shadow private long lastSyncSysClock;
    @Shadow private float tickLength;

    @Override
    public void setLastSyncSysClock(long lastSyncSysClock) {
        this.lastSyncSysClock = lastSyncSysClock;
    }

    @Override
    public long getLastSyncSysClock() {
        return lastSyncSysClock;
    }

    @Override
    public void setTickLength(float tickLength) {
        this.tickLength = tickLength;
    }

    @Override
    public float getTickLength() {
        return tickLength;
    }
}
