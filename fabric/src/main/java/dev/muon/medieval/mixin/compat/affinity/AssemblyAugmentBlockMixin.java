package dev.muon.medieval.mixin.compat.affinity;

import fuzs.visualworkbench.VisualWorkbench;
import io.wispforest.affinity.block.impl.AssemblyAugmentBlock;
import io.wispforest.affinity.blockentity.impl.AssemblyAugmentBlockEntity;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AssemblyAugmentBlock.class, remap = false)
public class AssemblyAugmentBlockMixin {

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true, remap = true)
    private void canPlaceAtVisualWorkbench(BlockState state, LevelReader world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState downState = world.getBlockState(pos.below());
        Block downBlock = downState.getBlock();

        if (VisualWorkbench.BLOCK_PREDICATE.test(downBlock)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "updateShape", at = @At("RETURN"), cancellable = true, remap = true)
    private void updateShapeVisualWorkbench(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        if (direction == Direction.DOWN && cir.getReturnValue() == Blocks.AIR.defaultBlockState()) {
            Block neighborBlock = neighborState.getBlock();
            if (VisualWorkbench.BLOCK_PREDICATE.test(neighborBlock)) {
                cir.setReturnValue(state);
            }
        }
    }

    @Shadow
    private static void openScreen(Player player, AssemblyAugmentBlockEntity augment) {}

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = true)
    private static void addVisualWorkbenchCallback(CallbackInfo ci) {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }

            BlockState state = world.getBlockState(hitResult.getBlockPos());
            Block block = state.getBlock();

            if (VisualWorkbench.BLOCK_PREDICATE.test(block) && !state.is(net.minecraft.world.level.block.Blocks.CRAFTING_TABLE)) {
                BlockEntity entity = world.getBlockEntity(hitResult.getBlockPos().above());
                if (entity instanceof AssemblyAugmentBlockEntity augment) {
                    if (!world.isClientSide()) {
                        openScreen(player, augment);
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            return InteractionResult.PASS;
        });
    }
}