package dev.muon.medieval.mixin.compat.dungeons_and_combat;

import net.mcreator.dungeonsandcombat.item.CombatStyleManualItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CombatStyleManualItem.class)
public class CombatStyleManualItemMixin {
    @Inject(method="use", at = @At("HEAD"), cancellable = true)
    private void disableBookUsage(Level world, Player entity, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        cir.setReturnValue(InteractionResultHolder.fail(entity.getItemInHand(hand)));
    }
}
