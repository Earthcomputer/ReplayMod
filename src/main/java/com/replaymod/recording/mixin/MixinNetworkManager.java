package com.replaymod.recording.mixin;

import com.replaymod.recording.ducks.INetworkManager;
import io.netty.channel.Channel;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NetworkManager.class)
public class MixinNetworkManager implements INetworkManager {

    @Shadow private Channel channel;

    @Override
    public Channel getChannel() {
        return channel;
    }
}
