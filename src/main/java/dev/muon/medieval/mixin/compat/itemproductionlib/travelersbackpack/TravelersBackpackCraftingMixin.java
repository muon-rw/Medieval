package dev.muon.medieval.mixin.compat.itemproductionlib.travelersbackpack;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.tiviacz.travelersbackpack.inventory.menu.TravelersBackpackBaseMenu;
import daripher.itemproduction.ItemProductionLib;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(value = TravelersBackpackBaseMenu.class, remap = false, priority = 1500)
public class TravelersBackpackCraftingMixin {
    @ModifyExpressionValue(
            method = "slotChangedCraftingGrid",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/Recipe;assemble(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;", remap = true)
    )
    private ItemStack modifyAssembledResult(ItemStack original, @Local(argsOnly = true) Player player) {
        return ItemProductionLib.itemProduced(original, player);
    }
}