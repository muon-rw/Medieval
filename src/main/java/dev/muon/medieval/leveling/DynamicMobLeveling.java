package dev.muon.medieval.leveling;

import daripher.autoleveling.event.MobsLevelingEvents;
import daripher.skilltree.capability.skill.PlayerSkillsProvider;
import dev.muon.medieval.Medieval;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Not currently implemented
 */
@Mod.EventBusSubscriber(modid = Medieval.MODID)
public class DynamicMobLeveling {

    private static final int UPDATE_INTERVAL = 200;
    private static final double LEVEL_RADIUS = 64.0;
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide()) {
            tickCounter++;
            if (tickCounter >= UPDATE_INTERVAL) {
                tickCounter = 0;
                updateMobLevels((ServerLevel) event.level);
            }
        }
    }

    private static void updateMobLevels(ServerLevel level) {
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof Mob) {
                entity.getCapability(OriginalLevelCapability.ORIGINAL_LEVEL).ifPresent(originalLevel -> {
                    int baseLevel = originalLevel.getLevel();
                    if (baseLevel == -1) {
                        baseLevel = MobsLevelingEvents.getLevel((LivingEntity) entity);
                        originalLevel.setLevel(baseLevel);
                    }
                    int additionalLevels = getAdditionalLevelsFromNearbyPlayers(level, (LivingEntity) entity);
                    int newLevel = baseLevel + additionalLevels;

                    MobsLevelingEvents.setLevel((LivingEntity) entity, newLevel);
                });
            }
        }
    }

    private static int getAdditionalLevelsFromNearbyPlayers(ServerLevel level, LivingEntity entity) {
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, entity.getBoundingBox().inflate(LEVEL_RADIUS));

        if (nearbyPlayers.isEmpty()) {
            return 0;
        }

        int totalSkillPoints = nearbyPlayers.stream()
                .filter(PlayerSkillsProvider::hasSkills)
                .mapToInt(player -> PlayerSkillsProvider.get(player).getPlayerSkills().size())
                .sum();

        int averageSkillPoints = totalSkillPoints / nearbyPlayers.size();
        return averageSkillPoints / 5;
    }
}