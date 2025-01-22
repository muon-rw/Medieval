package dev.muon.medieval.mixin.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.effect.EchoingStrikesEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EchoingStrikesEffect.class, remap = false)
public class EchoingStrikesEffectMixin {
    @Inject(method = "getDamageModifier", at = @At("HEAD"), cancellable = true)
    private static void lowerDamageModifier(int effectAmplifier, LivingEntity caster, CallbackInfoReturnable<Float> cir) {
        var power = caster == null ? 1 : SpellRegistry.ECHOING_STRIKES_SPELL.get().getEntityPowerMultiplier(caster);
        float newDamage = (((effectAmplifier - 4) * power * 0.02f) + 0.25f) * 0.1f;
        cir.setReturnValue(newDamage);
    }
}