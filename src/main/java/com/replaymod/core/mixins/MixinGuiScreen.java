package com.replaymod.core.mixins;

import com.replaymod.core.ducks.IGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(GuiScreen.class)
public class MixinGuiScreen implements IGuiScreen {

    @Shadow protected List<GuiButton> buttonList;

    @Override
    public List<GuiButton> getButtonList() {
        return buttonList;
    }
}
