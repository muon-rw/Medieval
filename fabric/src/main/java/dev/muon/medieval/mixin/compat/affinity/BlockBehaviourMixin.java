package dev.muon.medieval.mixin.compat.affinity;

import fuzs.visualworkbench.VisualWorkbench;
import io.wispforest.affinity.object.AffinityBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
    // DON'T DEAD
    // OPEN INSIDE
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void handleAssemblyAugmentPlacement(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                Player player, InteractionHand hand, BlockHitResult hitResult,
                                                CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (hitResult.getDirection() != Direction.UP) {
            return;
        }
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }
        if (blockItem.getBlock() != AffinityBlocks.ASSEMBLY_AUGMENT) {
            return;
        }
        Block block = state.getBlock();
        if (!VisualWorkbench.BLOCK_PREDICATE.test(block) &&
                !(block instanceof fuzs.visualworkbench.world.level.block.CraftingTableWithInventoryBlock)) {
            return;
        }
        BlockPos abovePos = pos.above();
        if (!level.getBlockState(abovePos).isAir()) {
            return;
        }
        BlockState augmentState = AffinityBlocks.ASSEMBLY_AUGMENT.defaultBlockState();
        if (level.setBlock(abovePos, augmentState, 11)) {
            SoundType soundType = augmentState.getSoundType();
            level.playSound(player, abovePos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            cir.setReturnValue(ItemInteractionResult.sidedSuccess(level.isClientSide));
        }
    }
}