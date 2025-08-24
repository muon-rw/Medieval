package dev.muon.medieval.mixin.compat.origins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
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
import java.util.Set;
import java.util.stream.Stream;

@Mixin(AttributeInstance.class)
public abstract class AttributeInstanceMixin implements OwnableAttributeInstance {

    @Shadow public abstract Set<AttributeModifier> getModifiers();
    @Shadow public abstract double getBaseValue();
    @Shadow public abstract Holder<Attribute> getAttribute();

    @Unique
    private List<Modifier> medieval$cachedPowerModifiers = null;

    @Unique
    private double medieval$lastBaseValue = Double.NaN;

    @Unique
    private int medieval$lastModifierHash = 0;

    @Unique
    private double medieval$cachedResult = Double.NaN;

    @ModifyReturnValue(method = "getValue", at = @At("RETURN"))
    private double medieval$optimizedModifyAttribute(double original) {
        Entity owner = this.apoli$getOwner();
        if (owner == null) {
            return original;
        }

        double baseValue = this.getBaseValue();
        int modifierHash = this.getModifiers().hashCode();

        // Check if cache is valid
        if (medieval$cachedPowerModifiers != null &&
                medieval$lastBaseValue == baseValue &&
                medieval$lastModifierHash == modifierHash) {

            // If no power modifiers, return original
            if (medieval$cachedPowerModifiers.isEmpty()) {
                return original;
            }

            // Return cached result if we have one
            if (!Double.isNaN(medieval$cachedResult)) {
                return medieval$cachedResult;
            }
        }

        // Update cache
        medieval$cachedPowerModifiers = PowerCache.getPowerTypes(owner, ModifyAttributePowerType.class)
                .stream()
                .filter(p -> p.getAttribute() == this.getAttribute())
                .flatMap(p -> p.getModifiers().stream())
                .toList();
        medieval$lastBaseValue = baseValue;
        medieval$lastModifierHash = modifierHash;

        // If no power modifiers, cache and return vanilla value
        if (medieval$cachedPowerModifiers.isEmpty()) {
            medieval$cachedResult = original;
            return original;
        }

        // Apply power modifiers and cache result
        List<Modifier> vanillaModifiers = this.getModifiers()
                .stream()
                .map(ModifierUtil::fromAttributeModifier)
                .toList();

        medieval$cachedResult = ModifierUtil.applyModifiers(
                owner,
                Stream.concat(medieval$cachedPowerModifiers.stream(), vanillaModifiers.stream()).toList(),
                baseValue
        );

        return medieval$cachedResult;
    }

    @Inject(method = "addModifier", at = @At("TAIL"))
    private void medieval$invalidateCacheOnAdd(AttributeModifier modifier, CallbackInfo ci) {
        medieval$cachedPowerModifiers = null;
        medieval$cachedResult = Double.NaN;
    }

    @Inject(method = "removeModifier", at = @At("TAIL"))
    private void medieval$invalidateCacheOnRemove(AttributeModifier modifier, CallbackInfo ci) {
        medieval$cachedPowerModifiers = null;
        medieval$cachedResult = Double.NaN;
    }
}