package dev.muon.medieval.mixin.compat.survive;

import com.stereowalker.survive.Survive;
import com.stereowalker.survive.needs.TemperatureData;
import com.stereowalker.survive.world.temperature.TemperatureModifier;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TemperatureData.class, remap = false)
public class TemperatureDataMixin {

    @Inject(method = "setTemperatureModifier*", at = @At("HEAD"), cancellable = true)
    private static void skipEnvTemp(LivingEntity entity, String id, double value, TemperatureModifier.ContributingFactor factor, CallbackInfo ci) {
        if (!Survive.TEMPERATURE_CONFIG.enabled) {
            ci.cancel();
        }
    }
}
