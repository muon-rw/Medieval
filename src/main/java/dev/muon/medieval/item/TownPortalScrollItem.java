package dev.muon.medieval.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class TownPortalScrollItem extends Item {
    private static final TagKey<Structure> VILLAGE_TAG = TagKey.create(Registries.STRUCTURE, new ResourceLocation("minecraft", "village"));
    private static final int CHANNEL_TICKS = 80;
    private static final int COOLDOWN_TICKS = 10 * 60 * 20; // 10 minutes
    private SoundInstance portalSound;

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

        // nvm this will never trigger lol
        if (player.getCooldowns().isOnCooldown(this)) {
            if (level.isClientSide) {
                int remainingCooldownSeconds = (int) (player.getCooldowns().getCooldownPercent(this, 0) * COOLDOWN_TICKS / 20);
                String formattedTime = String.format("%d:%02d", remainingCooldownSeconds / 60, remainingCooldownSeconds % 60);
                player.displayClientMessage(Component.translatable("item.medieval.town_portal_scroll.cooldown", formattedTime), true);
            }
            return InteractionResultHolder.fail(itemStack);
        }

        player.startUsingItem(hand);
        if (level.isClientSide) {
            playChannelSound(player);
        }
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
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide) {
            if (entity instanceof Player player) {
                player.displayClientMessage(Component.empty(), true);
            }
            stopChannelSound();
        }
    }

    private void playChannelSound(Player player) {
        SimpleSoundInstance sound = new SimpleSoundInstance(
                SoundEvents.PORTAL_TRIGGER,
                SoundSource.PLAYERS,
                0.5F,
                0.6F,
                player.getRandom(),
                player.blockPosition()
        );
        Minecraft.getInstance().getSoundManager().play(sound);
        this.portalSound = sound;
    }

    private void stopChannelSound() {
        if (this.portalSound != null) {
            Minecraft.getInstance().getSoundManager().stop(this.portalSound);
            this.portalSound = null;
        }
    }


    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return stack;
        }
        stopChannelSound();
        ServerLevel serverLevel = player.serverLevel();

        boolean teleported = false;
        Component resultMessage = null;

        // Check for valid respawn position
        if (player.getRespawnPosition() != null && player.getRespawnDimension() == serverLevel.dimension()) {
            Optional<Vec3> respawnPosition = Player.findRespawnPositionAndUseSpawnBlock(serverLevel, player.getRespawnPosition(), player.getRespawnAngle(), false, false);
            if (respawnPosition.isPresent()) {
                Vec3 pos = respawnPosition.get();
                player.teleportTo(serverLevel, pos.x(), pos.y(), pos.z(), player.getYRot(), player.getXRot());
                teleported = true;
            } else {
                player.setRespawnPosition(serverLevel.dimension(), null, 0.0F, false, false);
                resultMessage = Component.literal("Previous bed was missing or obstructed, teleporting to nearest village.");
            }
        }

        // No bed, try village
        if (!teleported) {
            BlockPos nearestVillage = serverLevel.findNearestMapStructure(VILLAGE_TAG, player.blockPosition(), 64, false);
            if (nearestVillage != null) {
                BlockPos safePos = findSafeSpawnLocation(serverLevel, nearestVillage);
                player.teleportTo(serverLevel, safePos.getX() + 0.5, safePos.getY() + 0.1, safePos.getZ() + 0.5, player.getYRot(), player.getXRot());
                teleported = true;
            }
        }



        if (teleported) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.75F);

            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        } else {
            resultMessage = Component.literal("No valid teleport destination found.");
        }

        if (resultMessage != null) {
            player.displayClientMessage(resultMessage, true);
        }

        return stack;
    }

    private BlockPos findSafeSpawnLocation(ServerLevel level, BlockPos start) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(start.getX(), level.getMaxBuildHeight(), start.getZ());

        while (mutable.getY() > level.getMinBuildHeight()) {
            BlockPos below = mutable.below();
            BlockState belowState = level.getBlockState(below);

            if (belowState.isSolid() || belowState.liquid()) {
                if (isSafeSpawn(level, mutable)) {
                    return mutable.immutable();
                }
                break;
            }

            mutable.move(Direction.DOWN);
        }

        return start;
    }

    private boolean isSafeSpawn(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir();
    }
}