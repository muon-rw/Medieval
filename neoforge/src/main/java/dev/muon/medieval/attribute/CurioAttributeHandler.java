package dev.muon.medieval.attribute;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class CurioAttributeHandler {

    public static Multimap<Holder<Attribute>, AttributeModifier> remapCurioAttributes(Multimap<Holder<Attribute>, AttributeModifier> original) {
        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();

        original.forEach((attributeHolder, modifier) -> {
            Holder<Attribute> remappedHolder = AttributeRemapper.getRemappedHolder(attributeHolder);

            double remappedValue = AttributeRemapper.getConvertedValue(attributeHolder, modifier.amount());
            AttributeModifier remappedModifier = new AttributeModifier(
                    modifier.id(),
                    remappedValue,
                    modifier.operation()
            );

            builder.put(remappedHolder, remappedModifier);
        });

        return builder.build();
    }
}