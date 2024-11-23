package dev.muon.medieval.mixin.compat.itemproductionlib.tetra;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import daripher.itemproduction.ItemProductionLib;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;

@Mixin(value = WorkbenchTile.class, remap = false)
public class WorkbenchTileMixin {
    @ModifyExpressionValue(
            method = "craft",
            at = @At(value = "INVOKE",
                    target = "Lse/mickelus/tetra/module/schematic/UpgradeSchematic;applyUpgrade(Lnet/minecraft/world/item/ItemStack;[Lnet/minecraft/world/item/ItemStack;ZLjava/lang/String;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack modifyUpgradedStack(ItemStack original) {
        BlockEntity self = (BlockEntity)(Object)this;
        return ItemProductionLib.itemProduced(original, self);
    }
}