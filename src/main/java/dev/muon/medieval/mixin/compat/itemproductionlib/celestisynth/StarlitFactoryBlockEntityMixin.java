package dev.muon.medieval.mixin.compat.itemproductionlib.celestisynth;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import daripher.itemproduction.ItemProductionLib;
import dev.muon.medieval.Medieval;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.thecelestialworkshop.celestisynth.common.block.StarlitFactoryBlockEntity;
import org.thecelestialworkshop.celestisynth.common.recipe.StarlitFactoryRecipe;

@Mixin(value = StarlitFactoryBlockEntity.class, remap = true)
public class StarlitFactoryBlockEntityMixin {

    @ModifyExpressionValue(
            method = "finishForging",
            at = @At(value = "INVOKE", target = "Lorg/thecelestialworkshop/celestisynth/common/recipe/StarlitFactoryRecipe;assemble(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;"),
            require = 1
    )
    private ItemStack modifyResultStack(ItemStack stack) {
        Medieval.LOGGER.info("Passing Starlit Factory recipe contents to IPL with ItemStack: {}}", Component.translatable(stack.getDescriptionId()));
        StarlitFactoryBlockEntity self = (StarlitFactoryBlockEntity)(Object)this;
        return ItemProductionLib.itemProduced(stack, self);
    }


}