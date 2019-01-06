package com.replaymod.core.mixins;

import com.replaymod.LiteModReplayMod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMainMenu.class)
public abstract class MixinGuiMainMenu extends GuiScreen {

    @Inject(method = "initGui", at = @At("TAIL"))
    public void postInitGui(CallbackInfo ci) {
        for (GuiButton button : this.buttonList) {
            // Buttons that aren't in a rectangle directly above our space don't need moving
            if (button.x + button.getButtonWidth() < this.width / 2 - 100
                    || button.x > this.width / 2 + 100
                    || button.y > this.height / 4 + 10 + 4 * 24) continue;
            // Move button up to make space for two rows of buttons
            // and then move back down by 10 to compensate for the space to the exit button that was already there
            button.y -= 2 * 24 - 10;
        }
        LiteModReplayMod.instance.injectIntoMainMenu((GuiMainMenu) (Object) this);
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    public void onActionPerformed(GuiButton button, CallbackInfo ci) {
        LiteModReplayMod.instance.onMainMenuActionPerformed((GuiMainMenu) (Object) this, button);
    }

}
