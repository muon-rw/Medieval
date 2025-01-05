package dev.muon.medieval;

import dev.muon.medieval.attribute.AttributeRemapper;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

import java.util.ArrayList;
import java.util.List;


@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class NeoEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void remapItemAttrModifiers(ItemAttributeModifierEvent event) {
        List<ItemAttributeModifiers.Entry> modifiers = event.getModifiers();

        new ArrayList<>(modifiers).forEach(entry -> {
            Holder<Attribute> sourceHolder = entry.attribute();
            Holder<Attribute> remappedHolder = AttributeRemapper.getRemappedHolder(sourceHolder);

            if (remappedHolder == sourceHolder) {
                return;
            }

            event.removeModifier(sourceHolder, entry.modifier().id());

            AttributeModifier remappedModifier = new AttributeModifier(
                    entry.modifier().id(),
                    AttributeRemapper.getConvertedValue(sourceHolder, entry.modifier().amount()),
                    entry.modifier().operation()
            );

            event.addModifier(remappedHolder, remappedModifier, entry.slot());
        });
    }
}