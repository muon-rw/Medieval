package dev.muon.medieval.mixin.compat.irons_spellbooks;

import dev.muon.medieval.ManaTextHelper;
import io.redspace.ironsspellbooks.network.casting.CastErrorPacket;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientSpellCastHelper.class,remap = false)
public class ClientSpellCastHelperMixin {
    // TODO: This doesn't even look like it's usually called.
    @Inject(method = "handleCastErrorMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;setOverlayMessage(Lnet/minecraft/network/chat/Component;Z)V", ordinal = 1))
    private static void onManaError(CastErrorPacket packet, CallbackInfo ci) {
        // TODO: Trigger mana amount text display on cast error
        // This is the resultant synced client method,
        // but it doesn't seem like its ever actually called
        // (The packet is only ever actually triggered for cooldowns)
    }
}