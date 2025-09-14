package dev.muon.medieval.mixin.compat.itemproductionlib.celestisynth;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import daripher.itemproduction.ItemProductionLib;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.thecelestialworkshop.celestisynth.common.block.StarlitFactoryBlockEntity;

@Mixin(value = StarlitFactoryBlockEntity.class, remap = false)
public class StarlitFactoryBlockEntityMixin {

    @ModifyExpressionValue(
            method = "finishForging",
            at = @At(value = "INVOKE", target = "Lorg/thecelestialworkshop/celestisynth/common/recipe/StarlitFactoryRecipe;assemble(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack modifyResultStack(ItemStack original) {
        BlockEntity self = (BlockEntity)(Object)this;
        return ItemProductionLib.itemProduced(original, self);
    }


}