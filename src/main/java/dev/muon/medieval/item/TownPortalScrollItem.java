package dev.muon.medieval.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class TownPortalScrollItem extends Item {
    private static final int CHANNEL_TICKS = 80;
    private static final int COOLDOWN_TICKS = 10 * 60 * 20;

    public TownPortalScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return CHANNEL_TICKS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.medieval.town_portal_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Cast Time: " + (CHANNEL_TICKS / 20.0) + "s")
                .withStyle(ChatFormatting.GREEN));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (level.dimension() != Level.OVERWORLD) {
            if (level.isClientSide) {
                player.displayClientMessage(Component.translatable("item.medieval.town_portal_scroll.wrong_dimension").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.fail(itemStack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemStack);
    }



    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide && livingEntity instanceof Player player) {
            int elapsedTicks = getUseDuration(stack) - remainingUseDuration;
            double remainingSeconds = Math.max(0, (CHANNEL_TICKS - elapsedTicks - 1) / 20.0);
            String formattedTime = String.format("%.1f", remainingSeconds);
            player.displayClientMessage(Component.literal("Channeling: " + formattedTime + "s"), true);
        }
    }



    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return stack;
        }

        ServerLevel serverLevel = player.serverLevel();
        BlockPos respawnPos = player.getRespawnPosition();
        ServerLevel respawnLevel = player.server.getLevel(player.getRespawnDimension());
        boolean teleported = false;
        Component resultMessage = null;

        if (respawnLevel != null && respawnPos != null) {
            Optional<Vec3> respawnLocation = findRespawnLocation(respawnLevel, respawnPos, player.getRespawnAngle(), player.isRespawnForced());
            if (respawnLocation.isPresent()) {
                Vec3 pos = respawnLocation.get();
                player.teleportTo(respawnLevel, pos.x, pos.y, pos.z, player.getYRot(), player.getXRot());
                teleported = true;
                resultMessage = Component.translatable("item.medieval.town_portal_scroll.teleport_bed").withStyle(ChatFormatting.GREEN);
            }
        }

        if (!teleported) {
            ServerLevel overworld = player.server.overworld();
            BlockPos worldSpawn = overworld.getSharedSpawnPos();
            player.teleportTo(overworld, worldSpawn.getX() + 0.5, worldSpawn.getY() + 0.1, worldSpawn.getZ() + 0.5, player.getYRot(), player.getXRot());
            resultMessage = Component.translatable("item.medieval.town_portal_scroll.teleport_world_spawn").withStyle(ChatFormatting.GREEN);
        }

        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.75F);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.displayClientMessage(resultMessage, true);
        return stack;
    }

    private Optional<Vec3> findRespawnLocation(ServerLevel level, BlockPos pos, float angle, boolean forced) {
        BlockState blockState = level.getBlockState(pos);
        Block block = blockState.getBlock();

        if (block instanceof RespawnAnchorBlock && (forced || blockState.getValue(RespawnAnchorBlock.CHARGE) > 0) && RespawnAnchorBlock.canSetSpawn(level)) {
            return RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, level, pos);
        } else if (block instanceof BedBlock && BedBlock.canSetSpawn(level)) {
            return BedBlock.findStandUpPosition(EntityType.PLAYER, level, pos, blockState.getValue(BedBlock.FACING), angle);
        } else if (forced) {
            boolean flag = block.isPossibleToRespawnInThis(blockState);
            BlockState blockState1 = level.getBlockState(pos.above());
            boolean flag1 = blockState1.getBlock().isPossibleToRespawnInThis(blockState1);
            if (flag && flag1) {
                return Optional.of(new Vec3(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5));
            }
        }

        return Optional.empty();
    }
}