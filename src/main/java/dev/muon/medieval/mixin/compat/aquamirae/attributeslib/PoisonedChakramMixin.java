package dev.muon.medieval.mixin.compat.aquamirae.attributeslib;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.obscuria.aquamirae.common.items.weapon.PoisonedChakraItem;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.UUID;

@Mixin(value = PoisonedChakraItem.class, remap = false)
public class PoisonedChakramMixin extends TieredItem {


    public PoisonedChakramMixin(Tier pTier, Properties pProperties) {
        super(pTier, pProperties);
    }

    /**
     * @author muon-rw
     * @reason Use AttributesLib modifiers to ensure affixed crit modifiers are grouped by Forge's merged tooltips feature
     */
    @Overwrite
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
        if (slot == EquipmentSlot.OFFHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.putAll(multimap);
            builder.put(ALObjects.Attributes.CRIT_CHANCE.get(), new AttributeModifier(UUID.fromString("A33F51D3-645C-4F38-A497-9C13A33DB5CF"), "Weapon modifier", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
            return builder.build();
        } else {
            return multimap;
        }
    }

}
