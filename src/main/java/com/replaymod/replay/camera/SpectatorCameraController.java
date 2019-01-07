package com.replaymod.replay.camera;

import com.replaymod.extras.ducks.IKeyBinding;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.ducks.IEntityLivingBase;
import com.replaymod.replay.ducks.IEntityPlayer;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Mouse;

import java.util.Arrays;

@RequiredArgsConstructor
public class SpectatorCameraController implements CameraController {
    private final CameraEntity camera;

    @Override
    public void update(float partialTicksPassed) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.keyBindSneak.isPressed()) {
            ReplayModReplay.instance.getReplayHandler().spectateCamera();
        }

        // Soak up all remaining key presses
        for (KeyBinding binding : Arrays.asList(mc.gameSettings.keyBindAttack, mc.gameSettings.keyBindUseItem,
                mc.gameSettings.keyBindJump, mc.gameSettings.keyBindSneak, mc.gameSettings.keyBindForward,
                mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight)) {
            ((IKeyBinding) binding).setPressTime(0);
        }

        // Prevent mouse movement
        Mouse.updateCursor();

        // Always make sure the camera is in the exact same spot as the spectated entity
        // This is necessary as some rendering code for the hand doesn't respect the view entity
        // and always uses mc.thePlayer
        Entity view = mc.getRenderViewEntity();
        if (view != null && view != camera) {
            camera.setCameraPosRot(mc.getRenderViewEntity());
            // If it's a player, also 'steal' its inventory so the rendering code knows what item to render
            if (view instanceof EntityPlayer) {
                EntityPlayer viewPlayer = (EntityPlayer) view;
                camera.inventory = viewPlayer.inventory;
                ((IEntityPlayer) camera).setItemStackMainHand(((IEntityPlayer) viewPlayer).getItemStackMainHand());
                camera.swingingHand = viewPlayer.swingingHand;
                ((IEntityLivingBase) camera).setItemInUseCount(viewPlayer.getItemInUseCount());
            }
        }
    }

    @Override
    public void increaseSpeed() {

    }

    @Override
    public void decreaseSpeed() {

    }
}
