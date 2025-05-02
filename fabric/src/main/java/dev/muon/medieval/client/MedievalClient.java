package dev.muon.medieval.client;

import dev.muon.medieval.client.combat.CombatMusicState;
import dev.muon.medieval.mixin.compat.reactivemusic.SongPickerMixin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class MedievalClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerEventCallbacks();
    }

    private void registerEventCallbacks() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide() && player instanceof Player && entity instanceof LivingEntity) {
                CombatMusicState.lastPlayerAttackTime = world.getGameTime();
            }
            return InteractionResult.PASS;
        });
    }
} 