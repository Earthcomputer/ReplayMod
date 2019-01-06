package com.replaymod.recording.mixin;

import com.replaymod.recording.ducks.ISPacketSpawnPlayer;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketSpawnPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SPacketSpawnPlayer.class)
public class MixinSPacketSpawnPlayer implements ISPacketSpawnPlayer {

    @Shadow private EntityDataManager watcher;

    @Override
    public void setWatcher(EntityDataManager watcher) {
        this.watcher = watcher;
    }

    @Override
    public EntityDataManager getWatcher() {
        return watcher;
    }
}
