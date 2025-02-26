package dev.muon.medieval.mixin.compat.spell_engine;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.spell_engine.internals.target.EntityRelation;
import net.spell_engine.internals.target.EntityRelations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = EntityRelations.class, remap = false)
public class EntityRelationsMixin {
    @ModifyReturnValue(method = "getRelation", at = @At("RETURN"))
    private static EntityRelation checkFTBTeams(EntityRelation original, LivingEntity attacker, Entity target) {
        if (!FTBTeamsAPI.api().isManagerLoaded()) {
            return original;
        }
        if (!(attacker instanceof ServerPlayer attackerPlayer && target instanceof ServerPlayer targetPlayer)) {
            return original;
        }

        var teamManager = FTBTeamsAPI.api().getManager();
        if (teamManager.arePlayersInSameTeam(attackerPlayer.getUUID(), targetPlayer.getUUID())) {
            return EntityRelation.ALLY;
        }
        return EntityRelation.MIXED;
    }
}