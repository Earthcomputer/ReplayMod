package com.replaymod.recording.ducks;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

public interface IResourcePackRepository {

    File getDirServerResourcepacks();

    ReentrantLock getLock();

    ListenableFuture<Object> getDownloadingPacks();

    void setDownloadingPacks(ListenableFuture<Object> downloadingPacks);

}
