package com.replaymod.recording.ducks;

import net.minecraft.network.datasync.EntityDataManager;

public interface ISPacketSpawnMob {

    void setDataManager(EntityDataManager dataManager);

    EntityDataManager getDataManager();

}
