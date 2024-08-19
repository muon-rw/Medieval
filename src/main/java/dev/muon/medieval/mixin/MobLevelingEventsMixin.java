package dev.muon.medieval.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import daripher.autoleveling.event.MobsLevelingEvents;
import dev.muon.medieval.Medieval;
import dev.muon.medieval.config.MedievalConfig;
import dev.muon.medieval.leveling.PlayerLevelHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

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

}

