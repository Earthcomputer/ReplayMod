package com.replaymod.extras.playeroverview;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.replaymod.LiteModReplayMod;
import com.replaymod.core.utils.Utils;
import com.replaymod.extras.Extra;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.camera.CameraEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.*;

public class PlayerOverview implements Extra {
    private ReplayModReplay module;

    public static PlayerOverview instance;
    public PlayerOverview() {
        instance = this;
    }

    private final Set<UUID> hiddenPlayers = new HashSet<>();
    private boolean savingEnabled;

    @Override
    public void register(final LiteModReplayMod mod) throws Exception {
        this.module = ReplayModReplay.instance;

        mod.getKeyBindingRegistry().registerKeyBinding("replaymod.input.playeroverview", Keyboard.KEY_B, () -> {
            if (module.getReplayHandler() != null) {
                @SuppressWarnings("unchecked")
                List<EntityPlayer> players = mod.getMinecraft().world.getPlayers(EntityPlayer.class, (Predicate) input -> {
                    return !(input instanceof CameraEntity); // Exclude the camera entity
                });
                if (!Utils.isCtrlDown()) {
                    // Hide all players that have an UUID v2 (commonly used for NPCs)
                    Iterator<EntityPlayer> iter = players.iterator();
                    while (iter.hasNext()) {
                        UUID uuid = iter.next().getGameProfile().getId();
                        if (uuid != null && uuid.version() == 2) {
                            iter.remove();
                        }
                    }
                }
                new PlayerOverviewGui(PlayerOverview.this, players).display();
            }
        });
    }

    public boolean isHidden(UUID uuid) {
        return hiddenPlayers.contains(uuid);
    }

    public void setHidden(UUID uuid, boolean hidden) {
        if (hidden) {
            hiddenPlayers.add(uuid);
        } else {
            hiddenPlayers.remove(uuid);
        }
    }

    @Override
    public void preReplayOpened(ReplayHandler handler) {
        Optional<Set<UUID>> savedData;
        try {
            savedData = handler.getReplayFile().getInvisiblePlayers();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (savedData.isPresent()) {
            hiddenPlayers.addAll(savedData.get());
            savingEnabled = true;
        } else {
            savingEnabled = false;
        }
    }

    @Override
    public void preReplayClosed(ReplayHandler handler) {
        hiddenPlayers.clear();
    }

    public boolean onRenderHand() {
        Entity view = module.getCore().getMinecraft().getRenderViewEntity();
        if (view != null && isHidden(view.getUniqueID())) {
            return false;
        }
        return true;
    }

    public boolean isSavingEnabled() {
        return savingEnabled;
    }

    public void setSavingEnabled(boolean savingEnabled) {
        this.savingEnabled = savingEnabled;
    }

    public void saveHiddenPlayers() {
        if (savingEnabled) {
            try {
                module.getReplayHandler().getReplayFile().writeInvisiblePlayers(hiddenPlayers);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
