package dev.muon.medieval.mixin.compat.reactivemusic;

import circuitlord.reactivemusic.SongPicker;
import circuitlord.reactivemusic.SongpackEventType;
import dev.muon.medieval.client.combat.CombatMusicState;
import dev.muon.medieval.config.MedievalConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = SongPicker.class, remap = false)
public class SongPickerMixin {

    @Unique
    private static final long MEDIEVAL_COMBAT_TIMEOUT_TICKS = 100;
    @Unique
    private static final TargetingConditions MEDIEVAL_COMBAT_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();

    /**
     * Checks if the player is considered "in combat".
     * This means they were recently damaged by a hostile mob, or a mob is actively targeting them.
     * (Attacking a mob is no longer sufficient on its own).
     */
    @Unique
    private static boolean medieval_isInCombat(LocalPlayer player, Level world) {
        if (player == null || world == null) {
            return false;
        }

        long currentTime = world.getGameTime();

        // Check 1: Recently damaged by hostile?
        if (CombatMusicState.lastPlayerDamagedByHostileTime > 0 && (currentTime - CombatMusicState.lastPlayerDamagedByHostileTime) < MEDIEVAL_COMBAT_TIMEOUT_TICKS) {
            return true;
        }

        // Check 2: Being targeted?
        double radius = MedievalConfig.CLIENT.combatMusicDetectionRadius.get();
        AABB combatCheckBox = new AABB(player.getX() - radius, player.getY() - 6.0, player.getZ() - radius,
                player.getX() + radius, player.getY() + 6.0, player.getZ() + radius);

        List<Monster> nearbyHostiles = world.getEntitiesOfClass(Monster.class, combatCheckBox,
                monster -> monster != null && monster.isAlive() && monster.canAttack(player, MEDIEVAL_COMBAT_TARGETING));

        // Also consider if the player's last damage source was a currently nearby hostile entity
        Entity lastAttacker = player.getLastDamageSource() != null ? player.getLastDamageSource().getDirectEntity() : null;
        if (lastAttacker instanceof Monster && nearbyHostiles.contains(lastAttacker)) {
            return true;
        }

        return !nearbyHostiles.isEmpty();
    }

    /**
     * Inject after the original NEARBY_MOBS check and overwrite its result
     * with our more specific combat logic.
     */
    @Inject(
            method = "tickEventMap()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;getBossOverlay()Lnet/minecraft/client/gui/components/BossHealthOverlay;",
                    shift = At.Shift.BEFORE,
                    remap = true
            ),
            remap = false
    )
    private static void medieval_overwriteNearbyMobsCheck(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level world = mc.level;
        if (player == null || world == null) {
            return;
        }

        boolean isInCombat = medieval_isInCombat(player, world);
        SongPicker.songpackEventMap.put(SongpackEventType.NEARBY_MOBS, isInCombat);
    }
}
