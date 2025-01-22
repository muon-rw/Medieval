package dev.muon.medieval.mixin.compat.ice_and_fire_spellbooks;

import net.acetheeldritchking.ice_and_fire_spellbooks.events.ServerEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerEvents.class, remap = false)
public class ServerEventsMixin {
    @Inject(method = "onLivingTickEvent", at = @At(value = "HEAD"), cancellable = true)
    private static void disableBadEventSubscriber(LivingEvent.LivingTickEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}