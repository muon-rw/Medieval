package dev.muon.medieval.mixin.compat.autoleveling;

import daripher.autoleveling.client.LevelPlateRenderer;
import net.minecraftforge.client.event.RenderNameTagEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelPlateRenderer.class, remap = false)
public abstract class LevelPlateRendererMixin {
    @Inject(method = "renderEntityLevel", at = @At("HEAD"), cancellable = true)
    private static void preventRender(RenderNameTagEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}