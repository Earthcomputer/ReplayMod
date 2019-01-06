package com.replaymod.recording.mixin;

import com.google.common.util.concurrent.ListenableFuture;
import com.replaymod.recording.ducks.IResourcePackRepository;
import net.minecraft.client.resources.ResourcePackRepository;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

@Mixin(ResourcePackRepository.class)
public class MixinResourcePackRepository implements IResourcePackRepository {

    @Shadow @Final private File dirServerResourcepacks;
    @Shadow @Final private ReentrantLock lock;
    @Shadow private ListenableFuture<Object> downloadingPacks;

    @Override
    public File getDirServerResourcepacks() {
        return dirServerResourcepacks;
    }

    @Override
    public ReentrantLock getLock() {
        return lock;
    }

    @Override
    public ListenableFuture<Object> getDownloadingPacks() {
        return downloadingPacks;
    }

    @Override
    public void setDownloadingPacks(ListenableFuture<Object> downloadingPacks) {
        this.downloadingPacks = downloadingPacks;
    }
}
