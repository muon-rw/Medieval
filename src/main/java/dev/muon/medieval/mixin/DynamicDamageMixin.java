package dev.muon.medieval.mixin;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.nameplate.NameplateMain;
import net.nameplate.access.MobEntityAccess;
import net.nameplate.network.NameplateServerPacket;
import net.nameplate.util.NameplateTracker;
import net.sweenus.simplyskills.SimplySkills;
import net.sweenus.simplyskills.util.DynamicDamage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(DynamicDamage.class)
public class DynamicDamageMixin {

    @Inject(method = "dynamicTotalAttributeScaling", at = @At("TAIL"))
    private static void onDynamicTotalAttributeScaling(LivingEntity entity, Attribute attribute, String name, double amount, UUID uuid, double pointsSpent, CallbackInfo ci) {
        if (entity instanceof Mob mob && attribute == Attributes.MAX_HEALTH && ((MobEntityAccess)mob).showMobRpgLabel()) {
            updateMobLevel(mob);
        }
    }

    @Unique
    private static void updateMobLevel(Mob mob) {
        double baseHealth = mob.getAttributeBaseValue(Attributes.MAX_HEALTH);
        double currentMaxHealth = mob.getMaxHealth();
        int newLevel = NameplateTracker.getMobLevel(mob);

        if (currentMaxHealth > baseHealth && newLevel == 1) {
            double healthMultiplier = currentMaxHealth / baseHealth;
            newLevel = Math.max(1, (int)(NameplateMain.CONFIG.levelMultiplier * healthMultiplier - NameplateMain.CONFIG.levelMultiplier + 1));
        }

        ((MobEntityAccess)mob).setMobRpgLevel(newLevel);
        notifyNearbyPlayers(mob, newLevel);
    }

    @Unique
    private static void notifyNearbyPlayers(Mob mob, int newLevel) {
        double checkDistance = SimplySkills.generalConfig.DASRadius;
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeVarInt(newLevel);
        data.writeVarInt(mob.getId());
        data.writeBoolean(((MobEntityAccess) mob).showMobRpgLabel());

        mob.level().players().stream()
                .filter(player -> player instanceof ServerPlayer)
                .map(player -> (ServerPlayer) player)
                .filter(serverPlayer -> serverPlayer.distanceToSqr(mob) <= checkDistance * checkDistance)
                .forEach(serverPlayer -> ServerPlayNetworking.send(serverPlayer, NameplateServerPacket.SET_MOB_LEVEL, data));
    }
}