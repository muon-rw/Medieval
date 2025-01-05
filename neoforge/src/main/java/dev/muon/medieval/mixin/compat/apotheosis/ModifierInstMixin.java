package dev.muon.medieval.mixin.compat.apotheosis;

import dev.muon.medieval.attribute.AttributeRemapper;
import dev.shadowsoffire.apotheosis.affix.AttributeAffix;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AttributeAffix.ModifierInst.class)
public class ModifierInstMixin {
    @Shadow(remap = false)
    @Final
    @Mutable
    private Holder<Attribute> attr;

    @Shadow(remap = false)
    @Final
    @Mutable
    private StepFunction valueFactory;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Holder<Attribute> attr, AttributeModifier.Operation op, StepFunction valueFactory, CallbackInfo ci) {
        Holder<Attribute> remappedAttr = AttributeRemapper.getRemappedHolder(this.attr);
        if (remappedAttr != this.attr) {
            this.attr = remappedAttr;
            float convertedMin = (float) AttributeRemapper.getConvertedValue(attr, valueFactory.min());
            float convertedStep = (float) AttributeRemapper.getConvertedValue(attr, valueFactory.step());
            float convertedMax = (float) AttributeRemapper.getConvertedValue(attr, valueFactory.max());
            this.valueFactory = new StepFunction(convertedMin, valueFactory.steps(), convertedStep, convertedMax);
        }
    }
}