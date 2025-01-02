package dev.muon.medieval.mixin.compat.dehydration;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.dehydration.DehydrationMain;
import net.dehydration.init.ConfigInit;
import net.dehydration.misc.ThirstTooltipData;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(value = ItemStack.class)
public abstract class ItemStackMixin {

    @ModifyReturnValue(
            method = "getTooltipImage",
            at = @At("RETURN")
    )
    private Optional<TooltipComponent> modifyTooltipImage(Optional<TooltipComponent> original) {
        ItemStack self = (ItemStack)(Object)this;
        if (!(self.getItem() instanceof PotionItem) || self.getItem() instanceof ThrowablePotionItem) {
            return original;
        }

        if (!ConfigInit.CONFIG.thirst_preview) {
            return Optional.empty();
        }

        int thirstQuench = 0;
        for (int i = 0; i < DehydrationMain.HYDRATION_TEMPLATES.size(); i++) {
            if (DehydrationMain.HYDRATION_TEMPLATES.get(i).containsItem(self.getItem())) {
                thirstQuench = DehydrationMain.HYDRATION_TEMPLATES.get(i).getHydration();
                break;
            }
        }

        if (thirstQuench == 0) {
            thirstQuench = ConfigInit.CONFIG.potion_thirst_quench;
        }

        PotionContents contents = self.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        Optional<Holder<Potion>> potionOptional = contents.potion();
        boolean isBad = potionOptional.map(this::isBadPotion).orElse(false);

        return Optional.of(new ThirstTooltipData(isBad ? 2 : 0, thirstQuench));
    }

    private boolean isBadPotion(Holder<Potion> potion) {
        return potion.is(Potions.WATER) || potion.is(Potions.AWKWARD) || potion.is(Potions.THICK) ||
                potion.is(Potions.HARMING) || potion.is(Potions.LONG_POISON) ||
                potion.is(Potions.LONG_SLOWNESS) || potion.is(Potions.LONG_WEAKNESS) ||
                potion.is(Potions.MUNDANE) || potion.is(Potions.POISON) || potion.is(Potions.SLOWNESS) ||
                potion.is(Potions.STRONG_HARMING) || potion.is(Potions.STRONG_POISON) ||
                potion.is(Potions.STRONG_SLOWNESS) || potion.is(Potions.WEAKNESS);
    }
}