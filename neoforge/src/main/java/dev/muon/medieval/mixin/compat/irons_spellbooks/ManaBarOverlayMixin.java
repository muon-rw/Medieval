package dev.muon.medieval.mixin.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.gui.overlays.ManaBarOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ManaBarOverlay.class)
public class ManaBarOverlayMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    public void hideManaBar(GuiGraphics guiHelper, DeltaTracker deltaTracker, CallbackInfo ci) {
        ci.cancel();
    }

}