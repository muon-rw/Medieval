package dev.muon.medieval.mixin.compat.travelersbackpack;

import com.llamalad7.mixinextras.sugar.Local;
import com.tiviacz.travelersbackpack.TravelersBackpack;
import com.tiviacz.travelersbackpack.inventory.menu.TravelersBackpackBaseMenu;
import com.tiviacz.travelersbackpack.network.ClientboundUpdateRecipePacket;
import daripher.itemproduction.ItemProductionLib;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TravelersBackpackBaseMenu.class, remap = false)
public class TravelersBackpackCraftingMixin {

    @Shadow
    public ResultContainer resultSlots;

    @Inject(
            method = "slotChangedCraftingGrid",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/network/simple/SimpleChannel;send(Lnet/minecraftforge/network/PacketDistributor$PacketTarget;Ljava/lang/Object;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void enhanceCraftingResult(Level level, Player player, CallbackInfo ci,
                                       @Local ItemStack itemstack,
                                       @Local(ordinal = 1) Recipe<CraftingContainer> recipe) {
        if (!level.isClientSide && recipe != null) {
            ItemStack enhancedResult = ItemProductionLib.itemProduced(itemstack, player);
            this.resultSlots.setItem(0, enhancedResult);

            TravelersBackpack.NETWORK.send(
                    PacketDistributor.PLAYER.with(() -> (ServerPlayer)player),
                    new ClientboundUpdateRecipePacket(recipe, enhancedResult)
            );
        }
    }
}