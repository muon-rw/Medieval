package dev.muon.medieval.mixin;

import com.stereowalker.survive.GuiHelper;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiHelper.class, remap = false)
public class GuiHelperMixin {

    // I love this mod!
    // =D
    @Inject(
            method = "registerOverlays",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/client/event/RegisterGuiOverlaysEvent;registerAbove(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/String;Lnet/minecraftforge/client/gui/overlay/IGuiOverlay;)V",
                    ordinal = 1
            ),
            cancellable = true
    )
    private static void cancelStaminaRendering(RegisterGuiOverlaysEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}