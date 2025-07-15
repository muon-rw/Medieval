package dev.muon.medieval.item;

import dev.muon.medieval.config.MedievalConfig;
import dev.muon.medieval.world.structure.StructureRegenerator;
import dev.muon.medieval.world.saved_data.StructureRegenerationState;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WayfindersMedallionItem extends Item {
    private static final int SEARCH_COOLDOWN = 25;
    private static final int COOLDOWN_TICKS = 20 * 5; // 5 seconds for wayfinder's medallion
    private static final int SEARCH_TIMEOUT_TICKS = 100;

    public WayfindersMedallionItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.medieval.wayfinders_medallion.tooltip")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.medieval.wayfinders_medallion.tooltip2")
                .withStyle(ChatFormatting.DARK_GRAY));
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
        StructureRegenerationState data = StructureRegenerationState.get(level);

        for (var structure : structureRegistry) {
            ResourceLocation structureId = structureRegistry.getKey(structure);
            if (structureId != null && !MedievalConfig.get().isStructureWhitelisted(structureId)) {
                continue;
            }

            player.displayClientMessage(Component.translatable("item.medieval.wayfinders_medallion.searching")
                    .withStyle(ChatFormatting.YELLOW), true);

            StructureStart nearestStart = StructureRegenerator.findNearestStructure(level, player.blockPosition(), structureId);
            if (nearestStart != null) {
                BlockPos structureCenter = nearestStart.getBoundingBox().getCenter();

                // Check if player already regenerated this specific structure instance
                if (data.hasPlayerRegeneratedStructure(player.getUUID(), structureId, structureCenter)) {
                    continue;
                }

                CompoundTag tag = itemStack.getOrCreateTag();
                tag.putString("FoundStructure", structureId.toString());
                tag.putLong("SearchTime", level.getGameTime());
                // Store the structure center so we can track the specific instance
                tag.putInt("StructureCenterX", structureCenter.getX());
                tag.putInt("StructureCenterZ", structureCenter.getZ());

                player.displayClientMessage(Component.translatable("item.medieval.wayfinders_medallion.found", structureId)
                        .withStyle(ChatFormatting.GREEN), true);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.0F, 2.0F);

                return;
            }
        }

        player.displayClientMessage(Component.translatable("item.medieval.wayfinders_medallion.not_found")
                .withStyle(ChatFormatting.RED), true);
    }

    private void confirmAndRegenerate(ServerLevel level, Player player, ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag == null || !tag.contains("FoundStructure")) {
            return;
        }

        ResourceLocation structureId = new ResourceLocation(tag.getString("FoundStructure"));
        // Get the stored structure center
        int centerX = tag.getInt("StructureCenterX");
        int centerZ = tag.getInt("StructureCenterZ");
        BlockPos structureCenter = new BlockPos(centerX, 0, centerZ);

        StructureRegenerationState data = StructureRegenerationState.get(level);
        long currentTime = level.getGameTime();

        // Check if this player already regenerated this specific structure instance
        if (data.hasPlayerRegeneratedStructure(player.getUUID(), structureId, structureCenter)) {
            player.displayClientMessage(Component.translatable("item.medieval.wayfinders_medallion.already_regenerated", structureId)
                    .withStyle(ChatFormatting.RED), true);
            tag.remove("FoundStructure");
            tag.remove("StructureCenterX");
            tag.remove("StructureCenterZ");
            return;
        }

        // Check for recent interactions
        if (data.hasRecentInteraction(structureId, currentTime, MedievalConfig.get().structureInactivityTimeoutSeconds)) {
            StructureRegenerationState.StructureInteraction interaction = data.getLastInteraction(structureId);
            if (interaction != null) {
                long timeSince = (currentTime - interaction.timestamp) / 20;
                player.displayClientMessage(Component.translatable("item.medieval.wayfinders_medallion.recent_activity",
                                structureId, interaction.type.name().toLowerCase(), timeSince)
                        .withStyle(ChatFormatting.YELLOW), false);
                player.displayClientMessage(Component.translatable("item.medieval.wayfinders_medallion.confirm_override")
                        .withStyle(ChatFormatting.GOLD), false);

                // Allow override with second use
                tag.putBoolean("OverrideActivity", true);
                tag.putLong("OverrideTime", currentTime);
                return;
            }
        }

        // Check if override was confirmed
        boolean overrideActivity = tag.getBoolean("OverrideActivity");
        long overrideTime = tag.getLong("OverrideTime");
        if (!overrideActivity || currentTime - overrideTime > 100) { // 5 second window to confirm
            tag.remove("OverrideActivity");
            tag.remove("OverrideTime");
        }

        // Check for players in structure (unless override is active)
        if (!overrideActivity) {
            StructureStart start = StructureRegenerator.findNearestStructure(level, player.blockPosition(), structureId);
            if (start != null && start.isValid() && hasOtherPlayersInStructure(level, player, start.getBoundingBox())) {
                player.displayClientMessage(Component.translatable("item.medieval.wayfinders_medallion.players_present", structureId)
                        .withStyle(ChatFormatting.RED), true);
                return;
            }
        }

        // Attempt regeneration
        player.displayClientMessage(Component.translatable("item.medieval.wayfinders_medallion.regenerating", structureId)
                .withStyle(ChatFormatting.YELLOW), true);

        StructureRegenerator.RegenerationResult result = StructureRegenerator.regenerateStructure(level, player.blockPosition(), structureId);

        if (result.success) {
            data.markStructureRegenerated(player.getUUID(), structureId, structureCenter);
            data.clearStructureInteraction(structureId);

            player.displayClientMessage(Component.translatable("item.medieval.wayfinders_medallion.success", result.message)
                    .withStyle(ChatFormatting.GREEN), true);
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            // Medallion is not consumed on use
        } else {
            player.displayClientMessage(Component.translatable("item.medieval.wayfinders_medallion.fail", result.message)
                    .withStyle(ChatFormatting.RED), true);
        }

        tag.remove("FoundStructure");
        tag.remove("StructureCenterX");
        tag.remove("StructureCenterZ");
        tag.remove("OverrideActivity");
        tag.remove("OverrideTime");
    }

    private boolean hasOtherPlayersInStructure(ServerLevel level, Player excludePlayer, BoundingBox boundingBox) {
        AABB aabb = new AABB(
                boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(),
                boundingBox.maxX() + 1, boundingBox.maxY() + 1, boundingBox.maxZ() + 1
        );

        List<Player> playersInStructure = level.getEntitiesOfClass(Player.class, aabb);

        // Check if any player other than the current one is in the structure
        return playersInStructure.stream()
                .anyMatch(p -> p != excludePlayer && p instanceof ServerPlayer);
    }
} 