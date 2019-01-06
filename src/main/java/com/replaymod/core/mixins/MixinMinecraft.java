package com.replaymod.core.mixins;

import com.replaymod.core.ducks.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.FutureTask;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IMinecraft {

    @Shadow @Final private Queue<FutureTask<?>> scheduledTasks;
    @Shadow @Final private List<IResourcePack> defaultResourcePacks;

    @Shadow protected abstract void resize(int width, int height);

    @Shadow @Final private Timer timer;

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
}
