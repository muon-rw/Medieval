package dev.muon.medieval.mixin.compat.origins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.muon.medieval.compat.origins.CachedModifiers;
import dev.muon.medievalorigins.util.PowerCache;
import io.github.apace100.apoli.access.OwnableAttributeInstance;
import io.github.apace100.apoli.power.type.ModifyAttributePowerType;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Stream;

@Mixin(AttributeInstance.class)
public abstract class AttributeInstanceMixin implements OwnableAttributeInstance {

    @Shadow public abstract Set<AttributeModifier> getModifiers();
    @Shadow public abstract double getBaseValue();
    @Shadow public abstract Holder<Attribute> getAttribute();

    @Unique
    private static final Map<AttributeInstance, CachedModifiers> medieval$modifierCache = new WeakHashMap<>();


    @ModifyReturnValue(method = "getValue", at = @At("RETURN"))
    private double medieval$modifyAttribute(double original) {
        Entity owner = this.apoli$getOwner();
        if (owner == null) {
            return original;
        }

        // Get or create cache entry
        CachedModifiers cache = medieval$modifierCache.computeIfAbsent(
                (AttributeInstance)(Object)this,
                k -> new CachedModifiers()
        );

        double baseValue = this.getBaseValue();
        int modifierHash = this.getModifiers().hashCode();

        // Check if cache is valid
        if (!cache.isValid(baseValue, modifierHash)) {
            // Update cache
            cache.powerModifiers = PowerCache.getPowerTypes(owner, ModifyAttributePowerType.class)
                    .stream()
                    .filter(p -> p.getAttribute() == this.getAttribute())
                    .flatMap(p -> p.getModifiers().stream())
                    .toList();
            cache.lastBaseValue = baseValue;
            cache.lastModifierHash = modifierHash;
        }

        // If no power modifiers, return vanilla value
        if (cache.powerModifiers == null || cache.powerModifiers.isEmpty()) {
            return original;
        }

        // Apply power modifiers
        List<Modifier> vanillaModifiers = this.getModifiers()
                .stream()
                .map(ModifierUtil::fromAttributeModifier)
                .toList();

        return ModifierUtil.applyModifiers(
                owner,
                Stream.concat(cache.powerModifiers.stream(), vanillaModifiers.stream()).toList(),
                baseValue
        );
    }

    // Invalidate cache when modifiers change
    @Inject(method = "addModifier", at = @At("TAIL"))
    private void medieval$invalidateCacheOnAdd(AttributeModifier modifier, CallbackInfo ci) {
        medieval$modifierCache.remove((AttributeInstance)(Object)this);
    }

    @Inject(method = "removeModifier", at = @At("TAIL"))
    private void medieval$invalidateCacheOnRemove(AttributeModifier modifier, CallbackInfo ci) {
        medieval$modifierCache.remove((AttributeInstance)(Object)this);
    }
}