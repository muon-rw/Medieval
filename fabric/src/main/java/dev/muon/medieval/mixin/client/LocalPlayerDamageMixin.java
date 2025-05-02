package dev.muon.medieval.mixin.client;

import dev.muon.medieval.client.combat.CombatMusicState;
import dev.muon.medieval.mixin.compat.reactivemusic.SongPickerMixin;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerDamageMixin {

    @Inject(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD")
    )
    private void medieval_onPlayerHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (player.level() != null && player.level().isClientSide()) {
            Entity attacker = source.getDirectEntity();
            if (attacker instanceof Monster) {
                CombatMusicState.lastPlayerDamagedByHostileTime = player.level().getGameTime();
            }
        }
    }
} 