package com.replaymod.replay;

import com.replaymod.LiteModReplayMod;
import com.replaymod.core.ducks.IMinecraft;
import com.replaymod.core.utils.WrappedTimer;
import com.replaymod.replay.camera.CameraController;
import com.replaymod.replay.camera.CameraEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Timer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class InputReplayTimer extends WrappedTimer {
    private final ReplayModReplay mod;
    private final Minecraft mc;
    
    public InputReplayTimer(Timer wrapped, ReplayModReplay mod) {
        super(wrapped);
        this.mod = mod;
        this.mc = mod.getCore().getMinecraft();
    }

    @Override
    public void updateTimer() {
        super.updateTimer();

        // If we are in a replay, we have to manually process key and mouse events as the
        // tick speed may vary or there may not be any ticks at all (when the replay is paused)
        if (mod.getReplayHandler() != null) {
            if (mc.currentScreen == null || mc.currentScreen.allowUserInput) {
                while (Mouse.next()) {
                    handleMouseEvent();
                }

                while (Keyboard.next()) {
                    handleKeyEvent();
                }
            } else {
                try {
                    mc.currentScreen.handleInput();
                } catch (IOException e) { // *SIGH*
                    e.printStackTrace();
                }
            }
        }
    }

    protected void handleMouseEvent() {
        // TODO: Forge support
        //if (ForgeHooksClient.postMouseEvent()) return;

        int button = Mouse.getEventButton() - 100;
        boolean pressed = Mouse.getEventButtonState();

        // Update key binding states
        KeyBinding.setKeyBindState(button, pressed);
        if (pressed) {
            KeyBinding.onTick(button);
        }

        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            ReplayHandler replayHandler = mod.getReplayHandler();
            if (replayHandler != null) {
                CameraEntity cameraEntity = replayHandler.getCameraEntity();
                if (cameraEntity != null) {
                    CameraController controller = cameraEntity.getCameraController();
                    while (wheel > 0) {
                        controller.increaseSpeed();
                        wheel--;
                    }
                    while (wheel < 0) {
                        controller.decreaseSpeed();
                        wheel++;
                    }
                }
            }
        }

        if (mc.currentScreen == null) {
            if (!mc.inGameHasFocus && Mouse.getEventButtonState()) {
                // Regrab mouse if the user clicks into the window
                mc.setIngameFocus();
            }
        } else {
            try {
                mc.currentScreen.handleMouseInput();
            } catch (IOException e) { // WHO IS RESPONSIBLE FOR THIS MESS?!?
                e.printStackTrace();
            }
        }

        // TODO: Forge support
        //FMLCommonHandler.instance().fireMouseInput();
    }

    protected void handleKeyEvent() {
        int key = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
        boolean pressed = Keyboard.getEventKeyState();

        KeyBinding.setKeyBindState(key, pressed);
        if (pressed) {
            KeyBinding.onTick(key);
        }

        // Still want to be able to create debug crashes ]:D
        IMinecraft imc = (IMinecraft) mc;
        if (imc.getDebugCrashKeyPressTime() > 0) {
            if (Minecraft.getSystemTime() - imc.getDebugCrashKeyPressTime() >= 6000L) {
                throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
            }

            if (!Keyboard.isKeyDown(Keyboard.KEY_F3) || !Keyboard.isKeyDown(Keyboard.KEY_C)) {
                imc.setDebugCrashKeyPressTime(-1);
            }
        } else if (Keyboard.isKeyDown(Keyboard.KEY_F3) && Keyboard.isKeyDown(Keyboard.KEY_C)) {
            imc.setDebugCrashKeyPressTime(Minecraft.getSystemTime());
        }

        // Twitch, screenshot, fullscreen, etc. (stuff that works everywhere)
        if (LiteModReplayMod.instance.onDispatchKeyPresses()) {
            mc.dispatchKeypresses();
        }

        if (pressed) {
            // This might be subject to change as vanilla shaders are still kinda unused in 1.8
            if (key == Keyboard.KEY_F4 && mc.entityRenderer != null) {
                mc.entityRenderer.switchUseShader();
            }

            if (mc.currentScreen != null) {
                try {
                    mc.currentScreen.handleKeyboardInput();
                } catch (IOException e) { // AND WHO THOUGHT THIS WAS A GREAT IDEA?
                    e.printStackTrace();
                }
            } else {
                if (key == Keyboard.KEY_ESCAPE) {
                    mc.displayInGameMenu();
                }

                // Following are a ton of vanilla keyboard shortcuts, some are removed as they're useless in the
                // replay viewer as of now
                // TODO Update maybe add new key bindings
                // TODO: Translate magic values to Keyboard.KEY_ constants

                if (key == 32 && Keyboard.isKeyDown(61) && mc.ingameGUI != null) {
                    mc.ingameGUI.getChatGUI().clearChatMessages(false);
                }

                if (key == 31 && Keyboard.isKeyDown(61)) {
                    mc.refreshResources();
                }

                if (key == 20 && Keyboard.isKeyDown(61)) {
                    mc.refreshResources();
                }

                if (key == 33 && Keyboard.isKeyDown(61)) {
                    boolean flag1 = Keyboard.isKeyDown(42) | Keyboard.isKeyDown(54);
                    mc.gameSettings.setOptionValue(GameSettings.Options.RENDER_DISTANCE, flag1 ? -1 : 1);
                }

                if (key == 30 && Keyboard.isKeyDown(61)) {
                    mc.renderGlobal.loadRenderers();
                }

                if (key == 48 && Keyboard.isKeyDown(61)) {
                    mc.getRenderManager().setDebugBoundingBox(!mc.getRenderManager().isDebugBoundingBox());
                }

                if (key == 25 && Keyboard.isKeyDown(61)) {
                    mc.gameSettings.pauseOnLostFocus = !mc.gameSettings.pauseOnLostFocus;
                    mc.gameSettings.saveOptions();
                }

                if (key == 59) {
                    mc.gameSettings.hideGUI = !mc.gameSettings.hideGUI;
                }

                if (key == 61) {
                    mc.gameSettings.showDebugInfo = !mc.gameSettings.showDebugInfo;
                    mc.gameSettings.showDebugProfilerChart = GuiScreen.isShiftKeyDown();
                }

                if (mc.gameSettings.keyBindTogglePerspective.isPressed()) {
                    mc.gameSettings.thirdPersonView = (mc.gameSettings.thirdPersonView + 1) % 3;

                    if (mc.entityRenderer != null) { // Extra check, not in vanilla code
                        if (mc.gameSettings.thirdPersonView == 0) {
                            mc.entityRenderer.loadEntityShader(mc.getRenderViewEntity());
                        } else if (mc.gameSettings.thirdPersonView == 1) {
                            mc.entityRenderer.loadEntityShader(null);
                        }
                    }
                }
            }

            // Navigation in the debug chart
            if (mc.gameSettings.showDebugInfo && mc.gameSettings.showDebugProfilerChart) {
                if (key == Keyboard.KEY_0) {
                    imc.doUpdateDebugProfilerName(0);
                }

                for (int i = 0; i < 9; ++i) {
                    if (key == 2 + i) {
                        imc.doUpdateDebugProfilerName(i + 1);
                    }
                }
            }
        }

        // TODO: Forge
        //FMLCommonHandler.instance().fireKeyInput();
    }
}
