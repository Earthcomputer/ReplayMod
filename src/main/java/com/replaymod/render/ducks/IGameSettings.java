package com.replaymod.render.ducks;

import net.minecraft.util.SoundCategory;

import java.util.Map;

public interface IGameSettings {

    Map<SoundCategory, Float> getSoundLevels();

    void setSoundLevels(Map<SoundCategory, Float> soundLevels);

}
