package dev.muon.medieval.leveling;

import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import daripher.autoleveling.event.MobsLevelingEvents;
import daripher.skilltree.capability.skill.IPlayerSkills;
import daripher.skilltree.capability.skill.PlayerSkillsProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

public class LevelingUtils {
    public static int getPlayerLevel(Player player) {
        if (ModList.get().isLoaded("skilltree") && PlayerSkillsProvider.hasSkills(player)) {
            IPlayerSkills skills = PlayerSkillsProvider.get(player);
            return Math.max(1, skills.getPlayerSkills().size());
        } else if (ModList.get().isLoaded("justlevelingfork")) {
            AptitudeCapability aptitude = AptitudeCapability.get(player);
            if (aptitude != null) {
                return Math.max(1, (int) Math.ceil(aptitude.getGlobalLevel() / 8.0)); // Todo: Maybe make this logarithmic instead of linear
            }
        }
        return 1;
    }

    public static int getEntityLevel(LivingEntity entity) {
        if (ModList.get().isLoaded("autoleveling")) {
            return MobsLevelingEvents.getLevel(entity) + 1;
        }
        return 0;
    }
}