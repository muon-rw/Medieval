package dev.muon.medieval.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.muon.medieval.attribute.AttributeRemapper;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEffect.class)
public abstract class MobEffectMixin {
    @ModifyReturnValue(
            method = "addAttributeModifier(Lnet/minecraft/core/Holder;Lnet/minecraft/resources/ResourceLocation;DLnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;)Lnet/minecraft/world/effect/MobEffect;",
            at = @At("RETURN")
    )
    private MobEffect remapAddAttributeModifierSimple(MobEffect original, @Local(argsOnly = true) Holder<Attribute> attribute, @Local(argsOnly = true) ResourceLocation id, @Local(argsOnly = true) double amount, @Local(argsOnly = true) AttributeModifier.Operation operation) {
        Holder<Attribute> remappedHolder = AttributeRemapper.getRemappedHolder(attribute);
        if (remappedHolder != attribute) {
            double convertedAmount = AttributeRemapper.getConvertedValue(attribute, amount);
            MobEffect remappedEffect = ((MobEffect) (Object) this).addAttributeModifier(remappedHolder, id, convertedAmount, operation);
            return remappedEffect;
        }
        return original;
    }

    @ModifyReturnValue(
            method = "addAttributeModifier(Lnet/minecraft/core/Holder;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;Lit/unimi/dsi/fastutil/ints/Int2DoubleFunction;)Lnet/minecraft/world/effect/MobEffect;",
            at = @At("RETURN")
    )
    private MobEffect remapAddAttributeModifierCurve(MobEffect original, @Local Holder<Attribute> attribute, @Local ResourceLocation id, @Local AttributeModifier.Operation operation, @Local Int2DoubleFunction curve) {
        Holder<Attribute> remappedHolder = AttributeRemapper.getRemappedHolder(attribute);
        if (remappedHolder != attribute) {
            it.unimi.dsi.fastutil.ints.Int2DoubleFunction convertedCurve = i -> AttributeRemapper.getConvertedValue(attribute, curve.get(i));
            MobEffect remappedEffect = ((MobEffect) (Object) this).addAttributeModifier(remappedHolder, id, operation, convertedCurve);
            return remappedEffect;
        }
        return original;
    }
}