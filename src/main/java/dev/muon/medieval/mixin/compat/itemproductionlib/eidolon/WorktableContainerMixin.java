package dev.muon.medieval.mixin.compat.itemproductionlib.eidolon;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import daripher.itemproduction.ItemProductionLib;
import elucent.eidolon.gui.WorktableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = WorktableContainer.class, remap = false)
public class WorktableContainerMixin {
    @ModifyExpressionValue(
            method = "updateCraftingResult",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/CraftingRecipe;assemble(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack modifyCraftingResult(ItemStack original, @Local(argsOnly = true) Player player) {
        return ItemProductionLib.itemProduced(original, player);
    }

    @ModifyExpressionValue(
            method = "updateCraftingResult",
            at = @At(value = "INVOKE",
                    target = "Lelucent/eidolon/recipe/WorktableRecipe;getResult()Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack modifyWorktableResult(ItemStack original, @Local(argsOnly = true) Player player) {
        return ItemProductionLib.itemProduced(original, player);
    }
}