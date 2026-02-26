package dev.muon.medieval.mixin.compat.survive;

import com.stereowalker.survive.world.item.CanteenItem;
import com.stereowalker.survive.world.item.SItems;
import com.stereowalker.survive.world.item.crafting.CanteenFillingRecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.stereowalker.survive.Survive;
import net.minecraft.world.level.Level;


@Mixin(value = CanteenFillingRecipe.class, remap = false)
public class CanteenFillingRecipeMixin {
    @Inject(method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z", at = @At("HEAD"), cancellable = true)
    private void medieval$fixMatchesLogic(CraftingContainer inv, Level worldIn, CallbackInfoReturnable<Boolean> cir) {
        Potion savedPotion = null;
        int bottles = 0;
        int canteens = 0;
        boolean nether = false;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() == SItems.CANTEEN) {
                canteens++;
            } else if (stack.getItem() == SItems.NETHERITE_CANTEEN) {
                canteens++;
                nether = true;
            } else if (stack.getItem() == Items.POTION) {
                if (savedPotion == null) {
                    savedPotion = PotionUtils.getPotion(stack);
                    bottles++;
                } else if (savedPotion.equals(PotionUtils.getPotion(stack))) {
                    bottles++;
                } else {
                    cir.setReturnValue(false);
                    return;
                }
            } else if (!stack.isEmpty()) {
                cir.setReturnValue(false);
                return;
            }
            if (bottles > Survive.THIRST_CONFIG.canteenFillAmount(nether)) {
                cir.setReturnValue(false);
                return;
            }
        }
        cir.setReturnValue(savedPotion != null && bottles <= Survive.THIRST_CONFIG.canteenFillAmount(nether) && canteens == 1);
    }
    @Inject(method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void medieval$preservePotionNbt(CraftingContainer inv, RegistryAccess ra, CallbackInfoReturnable<ItemStack> cir) {
        int count = 0;
        ItemStack firstPotion = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() == Items.POTION) {
                if (firstPotion.isEmpty()) {
                    firstPotion = stack;
                }
                count++;
            }
        }

        if (!firstPotion.isEmpty()) {
            Potion savedPotion = PotionUtils.getPotion(firstPotion);
            ItemStack filledCanteen = new ItemStack(SItems.FILLED_CANTEEN);

            CompoundTag nbt = firstPotion.getTag();
            if (nbt != null) {
                filledCanteen.setTag(nbt.copy());
            }

            ItemStack result = CanteenItem.addToCanteen(filledCanteen, count, savedPotion);
            cir.setReturnValue(result);
        }
    }
}
