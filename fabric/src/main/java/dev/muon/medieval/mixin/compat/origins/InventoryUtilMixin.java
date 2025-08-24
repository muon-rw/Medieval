package dev.muon.medieval.mixin.compat.origins;

import io.github.apace100.apoli.util.InventoryUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(InventoryUtil.class)
public class InventoryUtilMixin {
    @Inject(method = "forEachStack", at = @At("HEAD"), cancellable = true)
    private static void onlyIteratePlayers(Entity entity, Consumer<ItemStack> stackConsumer, CallbackInfo ci) {
        if (!(entity instanceof Player)) {
            ci.cancel();
        }
    }
}
