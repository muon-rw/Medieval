package dev.muon.medieval.mixin.compat.itemproductionlib.farmersdelight;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import daripher.itemproduction.ItemProductionLib;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.Container;
import net.minecraft.core.RegistryAccess;

@Mixin(value = SkilletBlockEntity.class, remap = false)
public class SkilletBlockEntityMixin {
    @WrapOperation(
            method = "cookAndOutputItems",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/CampfireCookingRecipe;assemble(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;", remap = true)
    )
    private ItemStack modifyResultStack(CampfireCookingRecipe instance, Container container, RegistryAccess registryAccess, Operation<ItemStack> original) {
        ItemStack originalResult = original.call(instance, container, registryAccess);
        BlockEntity self = (BlockEntity)(Object)this;
        return ItemProductionLib.itemProduced(originalResult, self);
    }
}