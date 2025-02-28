package dev.muon.medieval.mixin.compat.affinity;

import fuzs.visualworkbench.VisualWorkbench;
import io.wispforest.affinity.blockentity.impl.AssemblyAugmentBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AssemblyAugmentBlockEntity.class, remap = false)
public class AssemblyAugmentBlockEntityMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = true)
    private static void registerVisualWorkbenchFallback(CallbackInfo ci) {
        ItemStorage.SIDED.registerFallback((tableWorld, tablePos, state, blockEntity, context) -> {
            Block block = state.getBlock();
            if (VisualWorkbench.BLOCK_PREDICATE.test(block) && !state.is(Blocks.CRAFTING_TABLE)) {
                BlockEntity entity = tableWorld.getBlockEntity(tablePos.above());
                if (entity instanceof AssemblyAugmentBlockEntity augment) {
                    return ((AssemblyAugmentBlockEntityAccessor) augment).getStorage();
                }
            }
            return null;
        });
    }
}