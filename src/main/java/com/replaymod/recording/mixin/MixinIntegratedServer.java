package com.replaymod.recording.mixin;

import com.replaymod.recording.ducks.IIntegratedServer;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(IntegratedServer.class)
public class MixinIntegratedServer implements IIntegratedServer {

    @Shadow private boolean isGamePaused;

    @Override
    public boolean isGamePaused() {
        return isGamePaused;
    }
}
