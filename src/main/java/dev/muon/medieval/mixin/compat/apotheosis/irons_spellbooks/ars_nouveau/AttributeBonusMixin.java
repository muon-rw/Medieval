package dev.muon.medieval.mixin.compat.apotheosis.irons_spellbooks.ars_nouveau;

import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.AttributeBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = AttributeBonus.class, remap = false)
public class AttributeBonusMixin {
    @Shadow
    @Final
    @Mutable
    protected Attribute attribute;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void remapAttributeAffixes(GemClass gemClass, Attribute attr, AttributeModifier.Operation op, Map<LootRarity, StepFunction> values, CallbackInfo ci) {
        if (this.attribute == attr) {
            if (this.attribute == AttributeRegistry.MAX_MANA.get()) {
                this.attribute = PerkAttributes.MAX_MANA.get();
            }

            if (this.attribute == AttributeRegistry.MANA_REGEN.get()) {
                this.attribute = PerkAttributes.MANA_REGEN_BONUS.get();
            }
        }
    }
}
