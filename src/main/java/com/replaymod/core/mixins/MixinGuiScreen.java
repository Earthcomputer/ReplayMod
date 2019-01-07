package com.replaymod.core.mixins;

import com.replaymod.core.ducks.IGuiScreen;
import de.johni0702.minecraft.gui.container.VanillaGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.util.List;

@Mixin(GuiScreen.class)
public class MixinGuiScreen implements IGuiScreen {

    @Shadow protected List<GuiButton> buttonList;

    @Override
    public List<GuiButton> getButtonList() {
        return buttonList;
    }

    @Redirect(method = "handleInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleMouseInput()V"))
    public void redirectHandleMouseInput(GuiScreen _this) throws IOException {
        if (VanillaGuiScreen.onMouseInput())
            _this.handleMouseInput();
    }

    @Redirect(method = "handleInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleKeyboardInput()V"))
    public void redirectHandleKeyboardInput(GuiScreen _this) throws IOException {
        if (VanillaGuiScreen.onKeyboardInput())
            _this.handleKeyboardInput();
    }
}
