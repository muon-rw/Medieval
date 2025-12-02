package dev.muon.medieval.mixin.compat.witchery;

import dev.sterner.witchery.features.death.DeathTransformationHelper;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fixes a bug in Witchery where the player's selected hotbar slot is reset to 0
 * when taking damage due to armor durability changes triggering equipment change events.
 *
 * GitHub Issue: <a href="https://github.com/mrsterner/witchery/issues/74">...</a>
 */
@Mixin(value = DeathTransformationHelper.class, remap = false)
public class DeathTransformationHelperMixin {

    @Redirect(
            method = "updateDeathStatus",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;selected:I", opcode = org.objectweb.asm.Opcodes.PUTFIELD)
    )
    private void justDontDoThat(Inventory inventory, int value) {
        // yeet
    }
}

