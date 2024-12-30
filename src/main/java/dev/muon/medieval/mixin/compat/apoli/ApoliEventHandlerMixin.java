package dev.muon.medieval.mixin.compat.apoli;

import io.github.edwinmindcraft.apoli.common.ApoliEventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ApoliEventHandler.class, remap = false)
public class ApoliEventHandlerMixin {

    // The joys of developing for your own modpack
    // We don't use any of these features, and they are a massive performance overhead.

    @Inject(
            method = "livingTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/github/apace100/apoli/util/InventoryUtil;forEachStack(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Consumer;)V"
            ),
            cancellable = true
    )
    private static void skipNonPlayerInventoryProcessing(LivingEvent.LivingTickEvent event, CallbackInfo ci) {
        Entity entity = event.getEntity();
        if (entity == null || !(entity instanceof Player)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "attachItemCapabilities",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void preventCapabilityAttachment(AttachCapabilitiesEvent<ItemStack> event, CallbackInfo ci) {
        ci.cancel();
    }
}