package dev.muon.medieval.leveling;

import daripher.skilltree.capability.skill.PlayerSkillsProvider;
import dev.muon.medieval.config.MedievalConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.fml.ModList;

import java.util.List;
import java.util.Map;

public class PlayerLevelHelper {

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
            levelsToAdd += calculateEnchantmentPower(nearbyPlayers);
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

    /*
    private static int calculateEnchantmentPower(List<Player> players) {
        int totalEnchantmentLevels = players.stream()
                .flatMap(player -> player.getInventory().armor.stream())
                .flatMap(itemStack -> EnchantmentHelper.getEnchantments(itemStack).entrySet().stream())
                .mapToInt(Map.Entry::getValue)
                .sum();

        int averageEnchantmentLevels = totalEnchantmentLevels / players.size();
        return (int) (averageEnchantmentLevels * MedievalConfig.get().levelsPerEnchantmentLevel);
    }
    */
}