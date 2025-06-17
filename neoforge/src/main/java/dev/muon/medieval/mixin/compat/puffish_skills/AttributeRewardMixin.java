package dev.muon.medieval.mixin.compat.puffish_skills;

import dev.muon.medieval.attribute.AttributeRemapper;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.puffish.skillsmod.reward.builtin.AttributeReward;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AttributeReward.class)
public abstract class AttributeRewardMixin {

    @Shadow(remap = false)
    @Final
    @Mutable
    private Holder<Attribute> attribute;

    @Shadow(remap = false)
    @Final
    @Mutable
    private float value;

    @Inject(method = "<init>(Lnet/minecraft/core/Holder;FLnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;)V", at = @At("RETURN"), remap = false)
    private void remapAttributeRewards(Holder<Attribute> attribute, float value, AttributeModifier.Operation operation, CallbackInfo ci) {
        // We use this.attribute and this.value as they are the state of the object after its constructor has run.
        // The parameters attribute and value are the initial values passed to the constructor.
        Holder<Attribute> originalAttributeHolder = this.attribute; // This is the holder that was set by the original constructor
        Holder<Attribute> remappedHolder = AttributeRemapper.getRemappedHolder(originalAttributeHolder);

        if (remappedHolder != originalAttributeHolder) {
            float convertedValue = (float) AttributeRemapper.getConvertedValue(originalAttributeHolder, this.value);
            this.attribute = remappedHolder;
            this.value = convertedValue;
        }
    }
}
