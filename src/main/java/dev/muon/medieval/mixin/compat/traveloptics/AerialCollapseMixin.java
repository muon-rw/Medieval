package dev.muon.medieval.mixin.compat.traveloptics;

import com.gametechbc.traveloptics.spells.nature.AerialCollapseSpell;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AerialCollapseSpell.class, remap = false)
public class AerialCollapseMixin {
    @ModifyReturnValue(method = "getDamage", at = @At("RETURN"))
    private float lowerAndCapDamage(float original) {
        return Math.min(original * 0.2f, 35.0f);
    }
}