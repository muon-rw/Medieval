package dev.muon.medieval.mixin.compat.affinity;

import io.wispforest.affinity.blockentity.impl.AssemblyAugmentBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AssemblyAugmentBlockEntity.class, remap = false)
public interface AssemblyAugmentBlockEntityAccessor {
    @Accessor("storage")
    Storage<ItemVariant> getStorage();
}