package dev.muon.medieval.mixin.compat.aquamirae.attributeslib;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.obscuria.aquamirae.common.items.weapon.TerribleSwordItem;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.UUID;

@Mixin(value = TerribleSwordItem.class, remap = false)
public class TerribleSwordMixin extends SwordItem {

    public TerribleSwordMixin(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    /**
     * @author muon-rw
     * @reason Nerf this stupid ass item into the ground, and use AttributesLib modifiers to ensure affixed crit modifiers are grouped by Forge's merged tooltips feature
     */
    @Overwrite
    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot slot) {
        Multimap<Attribute, AttributeModifier> multimap = super.getDefaultAttributeModifiers(slot);
        if (slot == EquipmentSlot.MAINHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.putAll(multimap);
            builder.put(ALObjects.Attributes.CRIT_CHANCE.get(), new AttributeModifier(UUID.fromString("AB3F55D3-645C-4F38-A497-9C13A33DB5CF"), "Weapon modifier", 0.1F, AttributeModifier.Operation.MULTIPLY_BASE));
            builder.put(ALObjects.Attributes.CRIT_DAMAGE.get(), new AttributeModifier(UUID.fromString("AA3F55D3-645C-4F38-A497-9C13A33DB5CF"), "Weapon modifier", 0.5F, AttributeModifier.Operation.MULTIPLY_BASE));
            return builder.build();
        } else {
            return multimap;
        }
    }
}
