package com.replaymod.extras;

import com.replaymod.LiteModReplayMod;
import com.replaymod.replay.ReplayHandler;

public interface Extra {
    void register(LiteModReplayMod mod) throws Exception;

    default void beginTick() {}

    default void endTick() {}

    default void preReplayOpened(ReplayHandler handler) {}

    default void postReplayOpened(ReplayHandler handler) {}

    default void preReplayClosed(ReplayHandler handler) {}

    default void postReplayClosed(ReplayHandler handler) {}

    default boolean onDispatchKeyPresses() {
        return true;
    }
}
