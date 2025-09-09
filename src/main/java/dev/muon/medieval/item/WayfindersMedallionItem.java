package dev.muon.medieval.item;

import dev.muon.medieval.config.MedievalConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WayfindersMedallionItem extends Item {
    private static final int TOGGLE_COOLDOWN = 20; // 1 second cooldown for toggling

    public WayfindersMedallionItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isActive(stack);
    }

    public static boolean isActive(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean("Active");
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (isActive(stack)) {
            tooltip.add(Component.translatable("item.medieval.wayfinders_medallion.active")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("item.medieval.wayfinders_medallion.inactive")
                    .withStyle(ChatFormatting.GRAY));
        }

        tooltip.add(Component.translatable("item.medieval.wayfinders_medallion.tooltip")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.medieval.wayfinders_medallion.tooltip_equip")
                .withStyle(ChatFormatting.RED));

        if (!MedievalConfig.get().enableAutoRegeneration) {
            tooltip.add(Component.translatable("item.medieval.wayfinders_medallion.auto_regen_disabled")
                    .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (level.isClientSide) {
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        ItemStack itemStack = player.getItemInHand(hand);
        CompoundTag tag = itemStack.getOrCreateTag();

        // Toggle active state
        boolean wasActive = tag.getBoolean("Active");
        tag.putBoolean("Active", !wasActive);

        // Play sound effect
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                wasActive ? SoundEvents.BEACON_DEACTIVATE : SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS, 1.0F, 1.0F);

        // Send message to player
        if (!wasActive) {
            if (!MedievalConfig.get().enableAutoRegeneration) {
                player.displayClientMessage(Component.translatable("item.medieval.wayfinders_medallion.activated_but_disabled")
                        .withStyle(ChatFormatting.YELLOW), true);
            } else {
                player.displayClientMessage(Component.translatable("item.medieval.wayfinders_medallion.activated")
                        .withStyle(ChatFormatting.GREEN), true);
            }
        } else {
            player.displayClientMessage(Component.translatable("item.medieval.wayfinders_medallion.deactivated")
                    .withStyle(ChatFormatting.GRAY), true);
        }

        player.getCooldowns().addCooldown(this, TOGGLE_COOLDOWN);
        return InteractionResultHolder.success(itemStack);
    }


} 