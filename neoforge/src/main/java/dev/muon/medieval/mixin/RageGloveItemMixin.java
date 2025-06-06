package dev.muon.medieval.mixin;

import it.hurts.sskirillss.relics.items.relics.hands.RageGloveItem;
import net.mehvahdjukaar.dummmmmmy.common.TargetDummyEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RageGloveItem.Events.class, remap = false)
public class RageGloveItemMixin {
    @Inject(method = "onLivingHurt", at = @At("HEAD"), cancellable = true)
    private static void preventDummyExploit(LivingIncomingDamageEvent event, CallbackInfo ci) {
        if (event.getEntity() instanceof TargetDummyEntity) {
            ci.cancel();
        }
    }
}