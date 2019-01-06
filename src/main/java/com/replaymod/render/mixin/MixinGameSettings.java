package com.replaymod.render.mixin;

import com.replaymod.render.ducks.IGameSettings;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.SoundCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(GameSettings.class)
public class MixinGameSettings implements IGameSettings {
    @Shadow @Final private Map<SoundCategory, Float> soundLevels;

    @Override
    public Map<SoundCategory, Float> getSoundLevels() {
        return soundLevels;
    }

    @Override
    public void setSoundLevels(Map<SoundCategory, Float> soundLevels) {
        this.soundLevels.clear();
        this.soundLevels.putAll(soundLevels);
    }
}
