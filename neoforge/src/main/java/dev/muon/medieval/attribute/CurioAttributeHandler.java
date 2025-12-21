package dev.muon.medieval.attribute;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class CurioAttributeHandler {

    public static Multimap<Holder<Attribute>, AttributeModifier> remapCurioAttributes(Multimap<Holder<Attribute>, AttributeModifier> original) {
        // Must return a mutable Multimap - some implementations may call super.getAttributeModifiers()
        // and then mutate the result (e.g. Ars Nouveau AbstractManaCurio)
        Multimap<Holder<Attribute>, AttributeModifier> result = LinkedHashMultimap.create();

        original.forEach((attributeHolder, modifier) -> {
            Holder<Attribute> remappedHolder = AttributeRemapper.getRemappedHolder(attributeHolder);

            double remappedValue = AttributeRemapper.getConvertedValue(attributeHolder, modifier.amount());
            AttributeModifier remappedModifier = new AttributeModifier(
                    modifier.id(),
                    remappedValue,
                    modifier.operation()
            );

            result.put(remappedHolder, remappedModifier);
        });

        return result;
    }
}