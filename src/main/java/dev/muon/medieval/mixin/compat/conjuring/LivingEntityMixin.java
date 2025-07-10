package dev.muon.medieval.mixin.compat.conjuring;

import com.glisco.conjuring.Conjuring;
import com.glisco.conjuring.items.soul_alloy_tools.CopycatPlayerDamageSource;
import com.glisco.conjuring.items.soul_alloy_tools.SoulAlloyTool;
import com.glisco.conjuring.items.soul_alloy_tools.SoulAlloyToolAbilities;
import com.glisco.conjuring.util.ConjuringParticleEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow protected abstract void actuallyHurt(DamageSource damageSource, float damageAmount);

    @Unique
    private float damageReduction = -1;


    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    public void applySwordAoe(DamageSource source, float amount, CallbackInfo ci) {
        if (source instanceof CopycatPlayerDamageSource) return;
        if (!(source.getEntity() instanceof Player player)) return;

        if (!SoulAlloyToolAbilities.canAoeHit(player)) return;

        final int scopeLevel = SoulAlloyTool.getModifierLevel(player.getMainHandItem(), SoulAlloyTool.SoulAlloyModifier.SCOPE);
        final int range = 2 + scopeLevel;

        List<Entity> entities = level().getEntities(this, new AABB(position().subtract(range, 1, range), position().add(range, 1, range)), entity -> entity instanceof LivingEntity);

        entities.remove(player);

        for (int i = 0; i < Conjuring.CONFIG.tools_config.sword_scope_max_entities() && i < entities.size(); i++) {
            entities.get(i).hurt(new CopycatPlayerDamageSource(player), amount * Conjuring.CONFIG.tools_config.sword_scope_damage_multiplier() * scopeLevel);
            player.getMainHandItem().hurtAndBreak(4 * scopeLevel, player, (LivingEntity entity) -> player.broadcastBreakEvent(InteractionHand.MAIN_HAND));

            if (!level().isClientSide()) {
                ConjuringParticleEvents.LINE.spawn(level(), position(), new ConjuringParticleEvents.Line(
                        position().add(0, 0.25 + random.nextDouble(), 0),
                        entities.get(i).position().add(0, 0.25 + random.nextDouble(), 0)
                ));
            }
        }

    }

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    public void calculateDamageReduction(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!(source.getEntity() instanceof Player player)) return;

        if (!SoulAlloyToolAbilities.canArmorPierce(player)) return;

        float pierceDamage = SoulAlloyTool.getModifierLevel(player.getMainHandItem(), SoulAlloyTool.SoulAlloyModifier.IGNORANCE) * Conjuring.CONFIG.tools_config.sword_ignorance_multiplier() * amount;
        actuallyHurt(new CopycatPlayerDamageSource(player).pierceArmor(), pierceDamage);
        damageReduction = pierceDamage;
    }

    @ModifyVariable(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInvulnerableTo(Lnet/minecraft/world/damagesource/DamageSource;)Z", shift = At.Shift.AFTER), ordinal = 0)
    public float applyDamageReduction(float amount) {
        if (damageReduction == -1) return amount;

        float reductionCopy = damageReduction;
        damageReduction = -1;
        return amount - reductionCopy;
    }
}
