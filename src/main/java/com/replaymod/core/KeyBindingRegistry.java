package com.replaymod.core;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mumfrey.liteloader.core.LiteLoader;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import org.lwjgl.input.Keyboard;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class KeyBindingRegistry {
    private Map<String, KeyBinding> keyBindings = new HashMap<String, KeyBinding>();
    private Multimap<KeyBinding, Runnable> keyBindingHandlers = ArrayListMultimap.create();
    private Multimap<KeyBinding, Runnable> repeatedKeyBindingHandlers = ArrayListMultimap.create();
    private Multimap<Integer, Runnable> rawHandlers = ArrayListMultimap.create();

    public void registerKeyBinding(String name, int keyCode, Runnable whenPressed) {
        keyBindingHandlers.put(registerKeyBinding(name, keyCode), whenPressed);
    }

    public void registerRepeatedKeyBinding(String name, int keyCode, Runnable whenPressed) {
        repeatedKeyBindingHandlers.put(registerKeyBinding(name, keyCode), whenPressed);
    }

    private KeyBinding registerKeyBinding(String name, int keyCode) {
        KeyBinding keyBinding = keyBindings.get(name);
        if (keyBinding == null) {
            keyBinding = new KeyBinding(name, keyCode, "replaymod.title");
            keyBindings.put(name, keyBinding);
            LiteLoader.getInput().registerKeyBinding(keyBinding);
        }
        return keyBinding;
    }

    public void registerRaw(int keyCode, Runnable whenPressed) {
        rawHandlers.put(keyCode, whenPressed);
    }

    public Map<String, KeyBinding> getKeyBindings() {
        return Collections.unmodifiableMap(keyBindings);
    }

    public void onKeyInput() {
        handleKeyBindings();
        handleRaw();
    }

    public void onTick() {
        handleRepeatedKeyBindings();
    }

    public void handleRepeatedKeyBindings() {
        for (Map.Entry<KeyBinding, Collection<Runnable>> entry : repeatedKeyBindingHandlers.asMap().entrySet()) {
            if (entry.getKey().isKeyDown()) {
                invokeKeyBindingHandlers(entry.getKey(), entry.getValue());
            }
        }
    }

    public void handleKeyBindings() {
        for (Map.Entry<KeyBinding, Collection<Runnable>> entry : keyBindingHandlers.asMap().entrySet()) {
            while (entry.getKey().isPressed()) {
                invokeKeyBindingHandlers(entry.getKey(), entry.getValue());
            }
        }
    }

    private void invokeKeyBindingHandlers(KeyBinding keyBinding, Collection<Runnable> handlers) {
        for (final Runnable runnable : handlers) {
            try {
                runnable.run();
            } catch (Throwable cause) {
                CrashReport crashReport = CrashReport.makeCrashReport(cause, "Handling Key Binding");
                CrashReportCategory category = crashReport.makeCategory("Key Binding");
                category.addCrashSection("Key Binding", keyBinding);
                category.addDetail("Handler", runnable::toString);
                throw new ReportedException(crashReport);
            }
        }
    }

    public void handleRaw() {
        int keyCode = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
        for (final Runnable runnable : rawHandlers.get(keyCode)) {
            try {
                runnable.run();
            } catch (Throwable cause) {
                CrashReport crashReport = CrashReport.makeCrashReport(cause, "Handling Raw Key Binding");
                CrashReportCategory category = crashReport.makeCategory("Key Binding");
                category.addCrashSection("Key Code", keyCode);
                category.addDetail("Handler", runnable::toString);
                throw new ReportedException(crashReport);
            }
        }
    }
}
