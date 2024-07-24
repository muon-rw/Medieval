package dev.muon.medieval.item;

import dev.muon.medieval.StructureRegenerator;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ChallengeOrbItem extends Item {
    private static final int COOLDOWN_TICKS = 200;

    public ChallengeOrbItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (level.isClientSide) {
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ItemStack itemStack = player.getItemInHand(hand);
        CompoundTag tag = itemStack.getOrCreateTag();

        if (player.getCooldowns().isOnCooldown(this)) {
            player.displayClientMessage(Component.translatable("item.medieval.challenge_orb.cooldown")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(itemStack);
        }
        // First click
        if (!tag.contains("FoundStructure")) {
            findStructure(serverLevel, player, itemStack);
        } else {
            // Second click
            confirmAndRegenerate(serverLevel, player, itemStack);
        }

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
                    player.displayClientMessage(Component.translatable("item.medieval.challenge_orb.found", structureId)
                            .withStyle(ChatFormatting.GREEN), true);
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

            // Clear the found structure data
            tag.remove("FoundStructure");
        }
    }
}