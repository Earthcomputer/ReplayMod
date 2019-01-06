package com.replaymod.recording;

import com.replaymod.LiteModReplayMod;
import com.replaymod.recording.handler.ConnectionEventHandler;
import com.replaymod.recording.handler.RecordingEventHandler;
import com.replaymod.recording.packet.PacketListener;
import net.minecraft.network.NetworkManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class ReplayModRecording {
    public static ReplayModRecording instance;

    private LiteModReplayMod core;

    private Logger logger;

    private ConnectionEventHandler connectionEventHandler;

    public void init() {
        logger = LogManager.getLogger("replaymod-recording");
        core = LiteModReplayMod.instance;

        core.getSettingsRegistry().register(Setting.class);

        core.getKeyBindingRegistry().registerKeyBinding("replaymod.input.marker", Keyboard.KEY_M, () -> {
            PacketListener packetListener = connectionEventHandler.getPacketListener();
            if (packetListener != null) {
                packetListener.addMarker();
                core.printInfoToChat("replaymod.chat.addedmarker");
            }
        });

        connectionEventHandler = new ConnectionEventHandler(logger, core);
    }

    public void initiateRecording(NetworkManager networkManager) {
        connectionEventHandler.onConnectedToServerEvent(networkManager);
    }

    public ConnectionEventHandler getConnectionEventHandler() {
        return connectionEventHandler;
    }

    public RecordingEventHandler getRecordingEventHandler() {
        return connectionEventHandler.getRecordingEventHandler();
    }

    public void onDisconnected() {
        connectionEventHandler.onDisconnectedFromServerEvent();
    }
}
