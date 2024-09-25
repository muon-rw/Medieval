package dev.muon.medieval.leveling;

import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import daripher.autoleveling.event.MobsLevelingEvents;
import daripher.skilltree.capability.skill.IPlayerSkills;
import daripher.skilltree.capability.skill.PlayerSkillsProvider;
import dev.muon.medieval.config.MedievalConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

import java.util.List;

public class LevelingUtils {

    private static final int MAX_LEVEL = 20;
    private static final int START_LEVEL = 2;
    private static final int MIN_POINTS = 8;
    private static final int MAX_POINTS = 80;
    private static final int POINTS_PER_LEVEL = 4;

    public static int getPlayerLevel(Player player) {
        int level = START_LEVEL;
        if (ModList.get().isLoaded("skilltree") && PlayerSkillsProvider.hasSkills(player)) {
            IPlayerSkills skills = PlayerSkillsProvider.get(player);
            level = Math.max(START_LEVEL, START_LEVEL + skills.getPlayerSkills().size() - 1);
        } else if (ModList.get().isLoaded("justlevelingfork")) {
            AptitudeCapability aptitude = AptitudeCapability.get(player);
            if (aptitude != null) {
                int totalPoints = aptitude.getGlobalLevel();
                level = Math.min(MAX_LEVEL, START_LEVEL + (totalPoints - MIN_POINTS) / POINTS_PER_LEVEL);
            }
        }
        return level;
    }

    public static double getPlayerLevelProgress(Player player) {
        if (ModList.get().isLoaded("justlevelingfork")) {
            AptitudeCapability aptitude = AptitudeCapability.get(player);
            if (aptitude != null) {
                int totalPoints = aptitude.getGlobalLevel();
                if (totalPoints >= MAX_POINTS) {
                    return 100.0;
                }
                return ((totalPoints - MIN_POINTS) % POINTS_PER_LEVEL) / (double) POINTS_PER_LEVEL * 100.0;
            }
        }
        return 0.0;
    }

    public static int getEntityLevel(LivingEntity entity) {
        if (ModList.get().isLoaded("autoleveling")) {
            return MobsLevelingEvents.getLevel(entity) + 1;
        }
        return 0;
    }

    public static int getLevelsOfNearbyPlayers(ServerLevel level, LivingEntity entity) {
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, entity.getBoundingBox().inflate(MedievalConfig.get().levelingSearchRadius));

        if (nearbyPlayers.isEmpty()) {
            return 0;
        }
        int levelsToAdd = 0;

        if (ModList.get().isLoaded("skilltree")) {
            levelsToAdd += calculateSkillTreeLevels(nearbyPlayers);
        }
/*
        if (MedievalConfig.get().scaleWithEnchantPower) {
            levelsToAdd += PlayerEnchantLevel.calculateEnchantmentPower(nearbyPlayers);
        }
*/
        return levelsToAdd;
    }

    private static int calculateSkillTreeLevels(List<Player> players) {
        int totalSkillPoints = players.stream()
                .filter(PlayerSkillsProvider::hasSkills)
                .mapToInt(player -> PlayerSkillsProvider.get(player).getPlayerSkills().size())
                .sum();

        int averageSkillPoints = totalSkillPoints / players.size();
        return (int) (averageSkillPoints * MedievalConfig.get().levelsPerPoint);
    }
}