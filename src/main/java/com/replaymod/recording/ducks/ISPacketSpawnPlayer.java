package com.replaymod.recording.ducks;

import net.minecraft.network.datasync.EntityDataManager;

public interface ISPacketSpawnPlayer {

    void setWatcher(EntityDataManager watcher);

    EntityDataManager getWatcher();

}
