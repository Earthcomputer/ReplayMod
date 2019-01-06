package com.replaymod.pathing.player;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.replaymod.LiteModReplayMod;
import com.replaymod.core.ducks.IMinecraft;
import com.replaymod.core.ducks.ITimer;
import com.replaymod.core.utils.WrappedTimer;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.Timeline;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;

import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * Plays a timeline.
 */
public abstract class AbstractTimelinePlayer {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final ReplayHandler replayHandler;
    private Timeline timeline;
    protected long startOffset;
    private long lastTime;
    private long lastTimestamp;
    private ListenableFuture<Void> future;
    private SettableFuture<Void> settableFuture;

    public AbstractTimelinePlayer(ReplayHandler replayHandler) {
        this.replayHandler = replayHandler;
    }

    public ListenableFuture<Void> start(Timeline timeline, long from) {
        startOffset = from;
        return start(timeline);
    }

    public ListenableFuture<Void> start(Timeline timeline) {
        this.timeline = timeline;

        Iterator<Keyframe> iter = Iterables.concat(Iterables.transform(timeline.getPaths(),
                new Function<Path, Iterable<Keyframe>>() {
            @Nullable
            @Override
            public Iterable<Keyframe> apply(@Nullable Path input) {
                assert input != null;
                return input.getKeyframes();
            }
        })).iterator();
        if (!iter.hasNext()) {
            lastTimestamp = 0;
        } else {
            lastTimestamp = new Ordering<Keyframe>() {
                @Override
                public int compare(@Nullable Keyframe left, @Nullable Keyframe right) {
                    assert left != null;
                    assert right != null;
                    return Longs.compare(left.getTime(), right.getTime());
                }
            }.max(iter).getTime();
        }

        LiteModReplayMod.instance.addReplayTimerListener(this::onTick);
        replayHandler.getReplaySender().setSyncModeAndWait();
        lastTime = 0;
        IMinecraft imc = (IMinecraft) mc;
        imc.setTimer(new ReplayTimer(imc.getTimer()));
        ((ITimer) imc.getTimer()).setTickLength(WrappedTimer.DEFAULT_MS_PER_TICK);
        imc.getTimer().renderPartialTicks = imc.getTimer().elapsedTicks = 0;
        return future = settableFuture = SettableFuture.create();
    }

    public ListenableFuture<Void> getFuture() {
        return future;
    }

    public boolean isActive() {
        return future != null && !future.isDone();
    }

    public void onTick() {
        if (future.isDone()) {
            IMinecraft imc = (IMinecraft) mc;
            imc.setTimer(((ReplayTimer) imc.getTimer()).getWrapped());
            replayHandler.getReplaySender().setReplaySpeed(0);
            replayHandler.getReplaySender().setAsyncMode(true);
            LiteModReplayMod.instance.removeReplayTimerListener(this::onTick);
            return;
        }
        long time = getTimePassed();
        if (time > lastTimestamp) {
            time = lastTimestamp;
        }

        // Apply to timeline
        timeline.applyToGame(time, replayHandler);

        // Update minecraft timer
        long replayTime = replayHandler.getReplaySender().currentTimeStamp();
        if (lastTime == 0) {
            // First frame, no change yet
            lastTime = replayTime;
        }
        float timeInTicks = replayTime / 50f;
        float previousTimeInTicks = lastTime / 50f;
        float passedTicks = timeInTicks - previousTimeInTicks;
        Timer timer = ((IMinecraft) mc).getTimer();
        timer.renderPartialTicks += passedTicks;
        timer.elapsedTicks = (int) timer.renderPartialTicks;
        timer.renderPartialTicks -= timer.elapsedTicks;

        lastTime = replayTime;

        if (time >= lastTimestamp) {
            settableFuture.set(null);
        }
    }

    public abstract long getTimePassed();
}
