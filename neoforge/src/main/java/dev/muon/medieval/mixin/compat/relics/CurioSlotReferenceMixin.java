package dev.muon.medieval.mixin.compat.relics;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.muon.medieval.Medieval;
import it.hurts.sskirillss.relics.system.casts.slots.CurioSlotReference;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = CurioSlotReference.class, remap = false)
public abstract class CurioSlotReferenceMixin {

    @Shadow
    public abstract String getType();

    @Shadow
    public abstract int getIndex();

    /**
     * Catches NPE when curio slot type doesn't exist in player's inventory.
     * This seems to potentially be triggered by varying slot counts?
     * Very hard to tell what's going on
     */
    @WrapMethod(method = "gatherStack")
    private ItemStack catchMissingCurioType(Player player, Operation<ItemStack> original) {
        try {
            return original.call(player);
        } catch (NullPointerException e) {
            Medieval.LOG.warn("CurioSlotReference.gatherStack failed for curio type '{}' at index {} - slot type may not exist",
                    getType(), getIndex());
            return ItemStack.EMPTY;
        }
    }
}
