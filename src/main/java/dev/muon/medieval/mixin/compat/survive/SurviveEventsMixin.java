package dev.muon.medieval.mixin.compat.survive;

import com.stereowalker.survive.Survive;
import com.stereowalker.survive.core.TempMode;
import com.stereowalker.survive.events.SurviveEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(value = SurviveEvents.class, remap = false)
public class SurviveEventsMixin {

    /**
     * Force disable Survive's Temperature checks.
     * Big performance hit, and for some reason still calculates when disabled
     */

    @Inject(method = "getExactTemperature", at = @At("HEAD"), cancellable = true)
    private static void skipExactTemp(Level world, BlockPos pos, @Coerce Object type, CallbackInfoReturnable<Double> cir) {
        if (!Survive.TEMPERATURE_CONFIG.enabled) {
            cir.setReturnValue((double) Survive.DEFAULT_TEMP);
        }
    }

    @Inject(method = "getBlendedTemperature", at = @At("HEAD"), cancellable = true)
    private static void skipBlendedTemp(Level world, BlockPos mainPos, BlockPos blendPos, @Coerce Object type, CallbackInfoReturnable<Double> cir) {
        if (!Survive.TEMPERATURE_CONFIG.enabled) {
            cir.setReturnValue((double) Survive.DEFAULT_TEMP);
        }
    }

    @Inject(method = "getAverageTemperature", at = @At("HEAD"), cancellable = true)
    private static void skipAverageTemp(Level world, BlockPos pos, @Coerce Object type, int rangeInBlocks, TempMode mode, CallbackInfoReturnable<Float> cir) {
        if (!Survive.TEMPERATURE_CONFIG.enabled) {
            cir.setReturnValue(Survive.DEFAULT_TEMP);
        }
    }
}