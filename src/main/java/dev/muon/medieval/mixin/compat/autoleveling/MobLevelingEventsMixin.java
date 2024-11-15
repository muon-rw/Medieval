package dev.muon.medieval.mixin.compat.autoleveling;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import daripher.autoleveling.event.MobsLevelingEvents;
import dev.muon.medieval.Medieval;
import dev.muon.medieval.config.MedievalConfig;
import dev.muon.medieval.leveling.EnhancedEntityLevelingSettings;
import dev.muon.medieval.leveling.EnhancedEntityLevelingSettingsReloader;
import dev.muon.medieval.leveling.LevelingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = MobsLevelingEvents.class, remap = false)
public class MobLevelingEventsMixin {

    @Unique
    private static final TagKey<EntityType<?>> FIXED_LEVEL_ENTITIES = TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, Medieval.loc("fixed_level_entities"));
    @Unique
    private static final TagKey<EntityType<?>> PASSIVE_WHITELIST = TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, Medieval.loc("passive_whitelist"));


    @ModifyReturnValue(method = "canHaveLevel", at = @At("RETURN"))
    private static boolean cancelLevelsForPassives(boolean original, Entity entity) {
        if (original && entity instanceof Animal animal && MedievalConfig.get().cancelLevelsForPassives) {
            if (entity.getType().is(PASSIVE_WHITELIST)) {
                return true;
            }
            AttributeInstance attackDamageAttribute = animal.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackDamageAttribute == null || attackDamageAttribute.getValue() <= 0) {
                return false;
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "createLevelForEntity", at = @At("RETURN"))
    private static int applyAdditionalLevels(int original, LivingEntity entity, double distance) {
        if (entity.getType().is(FIXED_LEVEL_ENTITIES)) {
            return original;
        }

        int modifiedLevel = original;
        if (MedievalConfig.get().enableStructureLevelBonus) {
            modifiedLevel += getStructureLevelBonus(entity);
        }
        if (MedievalConfig.get().applyPlayerBasedLeveling) {
            modifiedLevel += LevelingUtils.getLevelsOfNearbyPlayers((ServerLevel) entity.level(), entity);
        }

        return modifiedLevel;
    }

    @Unique
    private static int getStructureLevelBonus(LivingEntity entity) {
        ServerLevel level = (ServerLevel) entity.level();
        BlockPos pos = entity.blockPosition();
        Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);

        for (Structure structure : structureRegistry) {
            StructureStart start = level.structureManager().getStructureAt(pos, structure);
            if (start != null && start.isValid()) {
                ResourceLocation structureId = structureRegistry.getKey(structure);
                if (structureId != null) {
                    return MedievalConfig.get().getStructureLevelBonus(structureId);
                }
            }
        }

        return 0;
    }

    @Inject(method = "applyAttributeBonuses", at = @At("HEAD"), cancellable = true)
    private static void applyEnhancedAttributeBonuses(LivingEntity entity, CallbackInfo ci) {
        EnhancedEntityLevelingSettings settings = EnhancedEntityLevelingSettingsReloader.getSettingsForEntity(entity.getType());

        if (settings != null && !settings.getAttributeModifiers().isEmpty()) {
            int level = MobsLevelingEvents.getLevel(entity);
            Medieval.LOGGER.debug("Applying enhanced attribute bonuses for entity: {} with level: {}", entity.getType().getDescription(), level);

            settings.getAttributeModifiers().forEach((attribute, modifier) -> {
                double scaledAmount = modifier.getAmount() * level;
                Medieval.LOGGER.debug("Applying modifier for attribute: {}, amount: {}, operation: {}",
                        attribute.toString(), scaledAmount, modifier.getOperation());
                applyAttributeBonusIfPossible(entity, attribute, scaledAmount, modifier.getOperation());
            });

            ci.cancel(); // If custom modifiers are defined for this entity, we don't want the config default modifiers to be applied.
        }

    }

    @Unique
    private static void applyAttributeBonusIfPossible(LivingEntity entity, Attribute attribute, double bonus, AttributeModifier.Operation operation) {
        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance == null) return;
        UUID modifierId = UUID.fromString("6a102cb4-d735-4cb7-8ab2-3d383219a44e");
        AttributeModifier existingModifier = attributeInstance.getModifier(modifierId);
        if (existingModifier != null && existingModifier.getAmount() == bonus && existingModifier.getOperation() == operation)
            return;
        if (existingModifier != null) attributeInstance.removeModifier(existingModifier);
        AttributeModifier newModifier = new AttributeModifier(modifierId, "Auto Leveling Bonus", bonus, operation);
        attributeInstance.addPermanentModifier(newModifier);
        if (attribute == Attributes.MAX_HEALTH) entity.heal(entity.getMaxHealth());
    }
}

