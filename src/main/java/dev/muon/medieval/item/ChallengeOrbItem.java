package dev.muon.medieval.item;

import dev.muon.medieval.StructureRegenerator;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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

public class ChallengeOrbItem extends Item {
    private static final int SEARCH_COOLDOWN = 25;
    private static final int COOLDOWN_TICKS = 20 * 30;
    private static final int SEARCH_TIMEOUT_TICKS = 100;


    public ChallengeOrbItem(Properties properties) {
        super(properties);
    }
    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.medieval.challenge_orb.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (level.isClientSide) {
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ItemStack itemStack = player.getItemInHand(hand);
        CompoundTag tag = itemStack.getOrCreateTag();

        if (!tag.contains("FoundStructure")) {
            findStructure(serverLevel, player, itemStack);
            player.getCooldowns().addCooldown(this, SEARCH_COOLDOWN);
            return InteractionResultHolder.success(itemStack);
        }

        long currentTime = serverLevel.getGameTime();
        long searchTime = tag.getLong("SearchTime");
        if (currentTime - searchTime > SEARCH_TIMEOUT_TICKS) {
            tag.remove("FoundStructure");
            tag.remove("SearchTime");
            findStructure(serverLevel, player, itemStack);
            return InteractionResultHolder.success(itemStack);
        }

        confirmAndRegenerate(serverLevel, player, itemStack);
        return InteractionResultHolder.success(itemStack);
    }


    private void findStructure(ServerLevel level, Player player, ItemStack itemStack) {
        var structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);

        for (var structure : structureRegistry) {
            ResourceLocation structureId = structureRegistry.getKey(structure);
            if (StructureRegenerator.isValidStructure(structureId)) {
                player.displayClientMessage(Component.translatable("item.medieval.challenge_orb.searching")
                        .withStyle(ChatFormatting.YELLOW), true);


                if (StructureRegenerator.findNearestStructure(level, player.blockPosition(), structureId) != null) {
                    CompoundTag tag = itemStack.getOrCreateTag();
                    tag.putString("FoundStructure", structureId.toString());
                    tag.putLong("SearchTime", level.getGameTime());

                    player.displayClientMessage(Component.translatable("item.medieval.challenge_orb.found", structureId)
                            .withStyle(ChatFormatting.GREEN), true);

                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.0F, 2.0F);

                    return;
                }
            }
        }

        player.displayClientMessage(Component.translatable("item.medieval.challenge_orb.not_found")
                .withStyle(ChatFormatting.RED), true);
    }
    private void confirmAndRegenerate(ServerLevel level, Player player, ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null && tag.contains("FoundStructure")) {
            ResourceLocation structureId = new ResourceLocation(tag.getString("FoundStructure"));
            player.displayClientMessage(Component.translatable("item.medieval.challenge_orb.regenerating", structureId)
                    .withStyle(ChatFormatting.YELLOW), true);

            StructureRegenerator.RegenerationResult result = StructureRegenerator.regenerateStructure(level, player.blockPosition(), structureId);

            if (result.success) {
                player.displayClientMessage(Component.translatable("item.medieval.challenge_orb.success", result.message)
                        .withStyle(ChatFormatting.GREEN), true);
                player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
                itemStack.shrink(1);
            } else {
                player.displayClientMessage(Component.translatable("item.medieval.challenge_orb.fail", result.message)
                        .withStyle(ChatFormatting.RED), true);
            }

            tag.remove("FoundStructure");
        }
    }
}