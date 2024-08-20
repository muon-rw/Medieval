package dev.muon.medieval.mixin.compat.itemproductionlib.toms_storage;

import com.tom.storagemod.tile.CraftingTerminalBlockEntity;
import daripher.itemproduction.ItemProductionLib;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftingTerminalBlockEntity.class, remap = false)
public abstract class CraftingTerminalBlockEntityMixin {

    @Shadow private ResultContainer craftResult;

    @Inject(method = "onCraftingMatrixChanged", at = @At("TAIL"))
    private void enhanceCraftingResult(CallbackInfo ci) {
        if (!this.craftResult.isEmpty()) {
            ItemStack result = this.craftResult.getItem(0);
            BlockEntity self = (BlockEntity)(Object)this;
            ItemStack enhancedResult = ItemProductionLib.itemProduced(result, self);
            this.craftResult.setItem(0, enhancedResult);
        }
    }
}