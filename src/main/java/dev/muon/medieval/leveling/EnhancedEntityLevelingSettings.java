package dev.muon.medieval.leveling;

import daripher.autoleveling.settings.EntityLevelingSettings;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Map;

public class EnhancedEntityLevelingSettings {
    private final EntityLevelingSettings original;
    private final Map<Attribute, AttributeModifier> attributeModifiers;

    public EnhancedEntityLevelingSettings(EntityLevelingSettings original, Map<Attribute, AttributeModifier> attributeModifiers) {
        this.original = original;
        this.attributeModifiers = attributeModifiers;
    }

    public EntityLevelingSettings getOriginal() {
        return original;
    }

    public Map<Attribute, AttributeModifier> getAttributeModifiers() {
        return attributeModifiers;
    }
}