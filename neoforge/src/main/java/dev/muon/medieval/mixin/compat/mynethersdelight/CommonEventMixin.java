package dev.muon.medieval.mixin.compat.mynethersdelight;


import com.llamalad7.mixinextras.sugar.Local;
import com.soytutta.mynethersdelight.common.events.CommonEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = CommonEvent.class, remap = false)
public class CommonEventMixin {

    @ModifyArg(method = "livingDie", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;die(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private static DamageSource preventNullDamageSource(DamageSource source, @Local(argsOnly = true) LivingDeathEvent event) {
        if (source == null && event.getEntity() instanceof Mob mob) {
            return mob.level().damageSources().generic();
        }
        return source;
    }
}
