package dev.muon.medieval.mixin;

import dev.shadowsoffire.apotheosis.compat.twilight.TreasureGoblinBonus;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import net.mehvahdjukaar.dummmmmmy.common.TargetDummyEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TreasureGoblinBonus.class, remap = false)
public class TreasureGoblinBonusMixin {
    @Inject(method = "doPostAttack", at = @At("HEAD"), cancellable = true)
    private void preventDummyExploit(GemInstance inst, LivingEntity user, Entity target, CallbackInfo ci) {
        if (target instanceof TargetDummyEntity) {
            ci.cancel();
        }
    }
}