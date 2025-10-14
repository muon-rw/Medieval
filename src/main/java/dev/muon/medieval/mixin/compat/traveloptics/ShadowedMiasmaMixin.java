package dev.muon.medieval.mixin.compat.traveloptics;


import com.gametechbc.traveloptics.spells.eldritch.ShadowedMiasmaSpell;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ShadowedMiasmaSpell.class, remap = false)
public class ShadowedMiasmaMixin {
    @ModifyExpressionValue(method = "onCast", at = @At(value = "INVOKE", target = "Lcom/gametechbc/traveloptics/spells/eldritch/ShadowedMiasmaSpell;getSpellPower(ILnet/minecraft/world/entity/Entity;)F"))
    private float lowerAndCap(float original) {
        return Math.min(original * 0.03f, 15.0f);
    }

    @ModifyExpressionValue(method = "getUniqueInfo", at = @At(value = "INVOKE", target = "Lcom/gametechbc/traveloptics/spells/eldritch/ShadowedMiasmaSpell;getSpellPower(ILnet/minecraft/world/entity/Entity;)F"))
    private float lowerAndCapTooltip(float original) {
        return Math.min(original * 0.03f, 15.0f);
    }
}
