package dev.muon.medieval.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.muon.medieval.Medieval;
import dev.muon.medieval.StructureRegenerator;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class ChallengeOrbItem extends Item {
    private static final int SEARCH_COOLDOWN = 25;
    private static final int COOLDOWN_TICKS = 20 * 30;
    private static final int SEARCH_TIMEOUT_TICKS = 100;

    public ChallengeOrbItem(Properties properties) {
        super(properties);
    }

    protected abstract ChallengeOrbData getData(ItemStack stack);

    protected abstract void setData(ItemStack stack, ChallengeOrbData data);

    public record ChallengeOrbData(String foundStructure, long searchTime) {
        public static final Codec<ChallengeOrbData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.optionalFieldOf("foundStructure", "").forGetter(ChallengeOrbData::foundStructure),
                        Codec.LONG.fieldOf("searchTime").forGetter(ChallengeOrbData::searchTime)
                ).apply(instance, ChallengeOrbData::new)
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.medieval.challenge_orb.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        Medieval.LOG.info("ChallengeOrbItem used by player: {}", player.getName().getString());

        if (level.isClientSide) {
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ItemStack itemStack = player.getItemInHand(hand);
        ChallengeOrbData data = getData(itemStack);

        if (data == null || data.foundStructure().isEmpty()) {
            findStructure(serverLevel, player, itemStack);
            player.getCooldowns().addCooldown(this, SEARCH_COOLDOWN);
            return InteractionResultHolder.success(itemStack);
        }


        long currentTime = serverLevel.getGameTime();

        if (currentTime - data.searchTime() > SEARCH_TIMEOUT_TICKS) {
            setData(itemStack, new ChallengeOrbData("", 0));
            findStructure(serverLevel, player, itemStack);
            return InteractionResultHolder.success(itemStack);
        }

        confirmAndRegenerate(serverLevel, player, itemStack);
        return InteractionResultHolder.success(itemStack);
    }

    private void findStructure(ServerLevel level, Player player, ItemStack itemStack) {
        var structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);

        player.displayClientMessage(Component.literal("Searching for nearby structure...").withStyle(ChatFormatting.YELLOW), true);

        for (var structure : structureRegistry) {
            ResourceLocation structureId = structureRegistry.getKey(structure);
            if (structureId != null && StructureRegenerator.isValidStructure(structureId)) {

                StructureStart nearestStructure = StructureRegenerator.findNearestStructure(level, player.blockPosition(), structureId);
                if (nearestStructure != null) {

                    setData(itemStack, new ChallengeOrbData(structureId.toString(), level.getGameTime()));
                    player.displayClientMessage(Component.literal("Found structure: " + structureId + ". Click again to regenerate.").withStyle(ChatFormatting.GREEN), true);

                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.0F, 2.0F);

                    return;
                } else {
                    Medieval.LOG.debug("No instance of structure {} found nearby", structureId);
                }
            } else {
                Medieval.LOG.debug("Structure type {} is not valid for regeneration", structureId);
            }
        }

        player.displayClientMessage(Component.literal("No regeneratable structure found nearby.").withStyle(ChatFormatting.RED), true);
    }

    private void confirmAndRegenerate(ServerLevel level, Player player, ItemStack itemStack) {
        ChallengeOrbData data = getData(itemStack);
        if (data != null && data.foundStructure() != null) {
            try {
                ResourceLocation structureId = ResourceLocation.parse(data.foundStructure());
                player.displayClientMessage(Component.literal("Regenerating structure: " + structureId).withStyle(ChatFormatting.YELLOW), true);
                StructureRegenerator.RegenerationResult result = StructureRegenerator.regenerateStructure(level, player.blockPosition(), structureId);

                if (result.success) {
                    player.displayClientMessage(Component.literal("Success! " + result.message).withStyle(ChatFormatting.GREEN), true);
                    player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
                    setData(itemStack, new ChallengeOrbData("", 0));
                    if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }
                } else {
                    player.displayClientMessage(Component.literal("Failed: " + result.message).withStyle(ChatFormatting.RED), true);
                }
            } catch (ResourceLocationException e) {
                Medieval.LOG.error("Invalid structure ID: {}", data.foundStructure(), e);
                player.displayClientMessage(Component.literal("Error: Invalid structure ID").withStyle(ChatFormatting.RED), true);
            }
        }
    }

}