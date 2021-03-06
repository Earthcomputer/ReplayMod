package com.replaymod.core.mixins;

import com.replaymod.LiteModReplayMod;
import com.replaymod.core.ducks.IMinecraft;
import de.johni0702.minecraft.gui.container.VanillaGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.Timer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.FutureTask;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IMinecraft {

    @Shadow @Final private Queue<FutureTask<?>> scheduledTasks;
    @Shadow @Final private List<IResourcePack> defaultResourcePacks;
    @Shadow @Final private Timer timer;
    @Shadow private ResourcePackRepository mcResourcePackRepository;

    @Shadow protected abstract void resize(int width, int height);

    @Shadow private boolean hasCrashed;

    @Shadow private long debugCrashKeyPressTime;

    @Shadow protected abstract void updateDebugProfilerName(int keyCount);

    @Override
    public Queue<FutureTask<?>> getScheduledTasks() {
        return scheduledTasks;
    }

    @Override
    public List<IResourcePack> getDefaultResourcePacks() {
        return defaultResourcePacks;
    }

    @Override
    public void setTimer(Timer timer) {
        Field timerField = null;
        for (Field field : getClass().getDeclaredFields()) {
            if (field.getType() == Timer.class) {
                timerField = field;
                break;
            }
        }
        assert timerField != null;
        timerField.setAccessible(true);

        try {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);

            modifiersField.set(timerField, timerField.getModifiers() & ~Modifier.FINAL);
            timerField.set(this, timer);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

    @Override
    public void doResize(int width, int height) {
        resize(width, height);
    }

    @Override
    public ResourcePackRepository getMcResourcePackRepository() {
        return mcResourcePackRepository;
    }

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    public void onLoadWorld(WorldClient world, String loadingMessage, CallbackInfo ci) {
        if (world == null) {
            LiteModReplayMod.instance.onUnloadWorld();
        }
    }

    @Override
    public boolean hasCrashed() {
        return hasCrashed;
    }

    @Override
    public void setDebugCrashKeyPressTime(long debugCrashKeyPressTime) {
        this.debugCrashKeyPressTime = debugCrashKeyPressTime;
    }

    @Override
    public long getDebugCrashKeyPressTime() {
        return debugCrashKeyPressTime;
    }

    @Override
    public void doUpdateDebugProfilerName(int keyCount) {
        updateDebugProfilerName(keyCount);
    }

    @Inject(method = "displayGuiScreen", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;"))
    public void onSetGui(GuiScreen newGui, CallbackInfo ci) {
        VanillaGuiScreen.onGuiClosed();
    }

    // Weird injections here to do the equivalent of injecting into the end of the while loop
    private boolean anyKeyPressed = false;

    @Inject(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKey()I", remap = false, ordinal = 0))
    private void loopTickKeyboard(CallbackInfo ci) {
        if (anyKeyPressed) {
            LiteModReplayMod.instance.onKeyInput();
        } else {
            anyKeyPressed = true;
        }
    }

    @Inject(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;processKeyBinds()V"))
    private void endTickKeyboard(CallbackInfo ci) {
        if (anyKeyPressed) {
            LiteModReplayMod.instance.onKeyInput();
            anyKeyPressed = false;
        }
    }
}
