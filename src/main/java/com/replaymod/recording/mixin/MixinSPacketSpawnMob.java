package com.replaymod.recording.mixin;

import com.replaymod.recording.ducks.ISPacketSpawnMob;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketSpawnMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SPacketSpawnMob.class)
public class MixinSPacketSpawnMob implements ISPacketSpawnMob {

    @Shadow private EntityDataManager dataManager;

    @Override
    public void setDataManager(EntityDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public EntityDataManager getDataManager() {
        return dataManager;
    }
}
