package dev.muon.medieval.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import daripher.autoleveling.event.MobsLevelingEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = MobsLevelingEvents.class, remap = false)
public class MobLevelingEventsMixin {

    @ModifyReturnValue(method = "canHaveLevel", at = @At("RETURN"))
    private static boolean cancelLevelsForPassives(boolean original, Entity entity) {
        if (original && entity instanceof Animal) {
            LivingEntity livingEntity = (LivingEntity) entity;
            AttributeInstance attackDamageAttribute = livingEntity.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackDamageAttribute == null || attackDamageAttribute.getValue() <= 0) {
                return false;
            }
        }
        return original;
    }
}
