package dev.muon.medieval.mixin.compat.dehydration;

import net.dehydration.DehydrationMain;
import net.dehydration.access.ThirstManagerAccess;
import net.dehydration.init.ConfigInit;
import net.dehydration.init.EffectInit;
import net.dehydration.misc.ThirstTooltipData;
import net.dehydration.thirst.ThirstManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = PotionItem.class)
public abstract class PotionItemMixin {

    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    public void onFinishUsing(ItemStack stack, Level world, LivingEntity user, CallbackInfoReturnable<ItemStack> info) {
        if (user instanceof Player player) {
            PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            Optional<Holder<Potion>> potionOptional = potionContents.potion();

            if (!world.isClientSide() && potionOptional.map(this::isBadPotion).orElse(false) &&
                    world.random.nextFloat() >= ConfigInit.CONFIG.potion_bad_thirst_chance) {
                player.addEffect(new MobEffectInstance(EffectInit.THIRST,
                        ConfigInit.CONFIG.potion_bad_thirst_duration, 0, false, false, true));
            }

            ThirstManager thirstManager = ((ThirstManagerAccess) player).getThirstManager();
            int thirstQuench = 0;

            for (int i = 0; i < DehydrationMain.HYDRATION_TEMPLATES.size(); i++) {
                if (DehydrationMain.HYDRATION_TEMPLATES.get(i).containsItem(stack.getItem())) {
                    thirstQuench = DehydrationMain.HYDRATION_TEMPLATES.get(i).getHydration();
                    break;
                }
            }

            if (thirstQuench == 0) {
                thirstQuench = ConfigInit.CONFIG.potion_thirst_quench;
            }

            thirstManager.add(thirstQuench);
        }
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