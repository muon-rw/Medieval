package dev.muon.medieval.mixin.compat.irons_spellbooks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.client.KnownClientPlayer;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(value = DamageSources.class, remap = false)
public class DamageSourcesMixin {

    @ModifyReturnValue(method = "Lio/redspace/ironsspellbooks/damage/DamageSources;isFriendlyFireBetween(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)Z", at = @At("RETURN"))
    private static boolean checkFTBTeam(boolean original,
                                        @Local(ordinal = 0, argsOnly = true) Entity attacker,
                                        @Local(ordinal = 1, argsOnly = true) Entity target) {
        if (ModList.get().isLoaded("ftbteams")) {
            if (attacker instanceof Player attackerPlayer && target instanceof Player targetPlayer) {
                if (attackerPlayer.level().isClientSide) {
                    return medieval$isRelatedServer(attackerPlayer, targetPlayer);
                } else {
                    return medieval$isRelatedClient(attackerPlayer, targetPlayer);
                }
            }
        }
        return original;
    }


    @Unique
    private static boolean medieval$isRelatedServer(Player attackerPlayer, Player targetPlayer) {
        if (!FTBTeamsAPI.api().isClientManagerLoaded()) {
            return false;
        }
        var manager = FTBTeamsAPI.api().getClientManager();

        Optional<KnownClientPlayer> attackerKnownPlayerOpt = manager.getKnownPlayer(attackerPlayer.getUUID());
        if (attackerKnownPlayerOpt.isEmpty()) {
            return false;
        }

        Optional<KnownClientPlayer> targetKnownPlayerOpt = manager.getKnownPlayer(targetPlayer.getUUID());
        if (targetKnownPlayerOpt.isEmpty()) {
            return false;
        }

        KnownClientPlayer attackerKnownPlayer = attackerKnownPlayerOpt.get();
        KnownClientPlayer targetKnownPlayer = targetKnownPlayerOpt.get();

        if (attackerKnownPlayer.teamId().equals(targetKnownPlayer.teamId())) {
            return true;
        }

        return false;
    }

    @Unique
    private static boolean medieval$isRelatedClient(Player attackerPlayer, Player targetPlayer) {
        if (!FTBTeamsAPI.api().isManagerLoaded()) {
            return false;
        }
        var manager = FTBTeamsAPI.api().getManager();

        if (manager.arePlayersInSameTeam(attackerPlayer.getUUID(), targetPlayer.getUUID())) {
            return true;
        }

        return false;
    }
}
