package dev.muon.medieval.mixin.compat.autoleveling;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import daripher.autoleveling.config.Config;
import daripher.autoleveling.event.MobsLevelingEvents;
import dev.muon.medieval.Medieval;
import dev.muon.medieval.config.MedievalConfig;
import dev.muon.medieval.leveling.EnhancedEntityLevelingSettings;
import dev.muon.medieval.leveling.EnhancedEntityLevelingSettingsReloader;
import dev.muon.medieval.leveling.PlayerLevelHelper;
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
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

@Mixin(value = MobsLevelingEvents.class, remap = false)
public class MobLevelingEventsMixin {

    @Unique
    private static final TagKey<EntityType<?>> FIXED_LEVEL_ENTITIES = TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, Medieval.loc("fixed_level_entities"));
    @Unique
    private static final TagKey<EntityType<?>> PASSIVE_WHITELIST = TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, Medieval.loc("passive_whitelist"));


    @ModifyReturnValue(method = "canHaveLevel", at = @At("RETURN"))
    private static boolean cancelLevelsForPassives(boolean original, Entity entity) {
        if (MedievalConfig.get().cancelLevelsForPassives && original && entity instanceof Animal) {
            if (entity.getType().is(PASSIVE_WHITELIST)) {
                return true;
            }
            LivingEntity livingEntity = (LivingEntity) entity;
            AttributeInstance attackDamageAttribute = livingEntity.getAttribute(Attributes.ATTACK_DAMAGE);
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
            modifiedLevel += PlayerLevelHelper.getLevelsOfNearbyPlayers((ServerLevel) entity.level(), entity);
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

    /**
     * @author muon-rw
     * @reason Replacing the original method entirely, to ensure duplicate logic doesn't attempt to run
     */
    @Overwrite
    public static void applyAttributeBonuses(LivingEntity entity) {
        int level = MobsLevelingEvents.getLevel(entity);
        Medieval.LOGGER.info("Applying attribute bonuses for entity: {} with level: {}", entity.getType().toString(), level);

        EnhancedEntityLevelingSettings settings = EnhancedEntityLevelingSettingsReloader.getSettingsForEntity(entity.getType());

        if (settings != null) {
            Medieval.LOGGER.info("Enhanced settings found for entity: {}", entity.getType().toString());
            if (!settings.getAttributeModifiers().isEmpty()) {
                Medieval.LOGGER.info("Applying custom attribute modifiers for entity: {}", entity.getType().toString());
                settings.getAttributeModifiers().forEach((attribute, modifier) -> {
                    double scaledAmount = modifier.getAmount() * level;
                    Medieval.LOGGER.info("Applying modifier for attribute: {}, amount: {}, operation: {}",
                            attribute.toString(), scaledAmount, modifier.getOperation());
                    applyAttributeBonusIfPossible(entity, attribute, scaledAmount, modifier.getOperation());
                });
            } else {
                Medieval.LOGGER.warn("Enhanced settings found, but attribute modifiers are empty for entity: {}", entity.getType().toString());
            }
        } else {
            Medieval.LOGGER.info("No enhanced settings found for entity: {}. Falling back to config bonuses.", entity.getType().toString());
            Config.getAttributeBonuses()
                    .forEach((attribute, bonus) -> {
                        double scaledAmount = bonus * level;
                        Medieval.LOGGER.info("Applying config bonus for attribute: {}, amount: {}",
                                attribute.toString(), scaledAmount);
                        applyAttributeBonusIfPossible(entity, attribute, scaledAmount, AttributeModifier.Operation.MULTIPLY_TOTAL);
                    });
        }
    }
    @Unique
    private static void applyAttributeBonusIfPossible(LivingEntity entity, Attribute attribute, double bonus, AttributeModifier.Operation operation) {
        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance == null) return;
        UUID modifierId = UUID.fromString("6a102cb4-d735-4cb7-8ab2-3d383219a44e");
        AttributeModifier existingModifier = attributeInstance.getModifier(modifierId);
        if (existingModifier != null && existingModifier.getAmount() == bonus && existingModifier.getOperation() == operation) return;
        if (existingModifier != null) attributeInstance.removeModifier(existingModifier);
        AttributeModifier newModifier = new AttributeModifier(modifierId, "Auto Leveling Bonus", bonus, operation);
        attributeInstance.addPermanentModifier(newModifier);
        if (attribute == Attributes.MAX_HEALTH) entity.heal(entity.getMaxHealth());
    }
}

