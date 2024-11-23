package dev.muon.medieval.mixin.compat.itemproductionlib.wizards_reborn;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import daripher.itemproduction.ItemProductionLib;
import mod.maxbogomol.wizards_reborn.common.block.arcane_workbench.ArcaneWorkbenchBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ArcaneWorkbenchBlockEntity.class, remap = false)
public class ArcaneWorkbenchMixin {
    @ModifyExpressionValue(
            method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lmod/maxbogomol/wizards_reborn/common/recipe/ArcaneWorkbenchRecipe;getResultItem(Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack modifyResultStack(ItemStack original) {
        BlockEntity self = (BlockEntity)(Object)this;
        return ItemProductionLib.itemProduced(original, self);
    }

    @ModifyExpressionValue(
            method = "getItemsResult",
            at = @At(value = "INVOKE",
                    target = "Lmod/maxbogomol/wizards_reborn/common/recipe/ArcaneWorkbenchRecipe;getResultItem(Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack modifyResultStackPreview(ItemStack original) {
        BlockEntity self = (BlockEntity)(Object)this;
        return ItemProductionLib.itemProduced(original, self);
    }
}