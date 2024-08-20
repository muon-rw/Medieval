package dev.muon.medieval.mixin.compat.itemproductionlib.celestisynth;

import com.aqutheseal.celestisynth.client.gui.celestialcrafting.CelestialCraftingMenu;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import daripher.itemproduction.ItemProductionLib;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = CelestialCraftingMenu.class, remap = false)
public class CelestialCraftingMenuMixin {

    @ModifyExpressionValue(
            method = "slotChangedCraftingGrid",
            at = @At(value = "INVOKE", target = "Lcom/aqutheseal/celestisynth/common/recipe/celestialcrafting/CelestialCraftingRecipe;assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;")
    )
    private static ItemStack modifyResultStack(ItemStack original, @Local(argsOnly = true) Player pPlayer) {
        return ItemProductionLib.itemProduced(original, pPlayer);
    }
}