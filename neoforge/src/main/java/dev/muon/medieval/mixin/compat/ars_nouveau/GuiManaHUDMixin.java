package dev.muon.medieval.mixin.compat.ars_nouveau;

import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.client.ClientInfo;
import com.hollingsworth.arsnouveau.client.gui.GuiManaHUD;
import com.hollingsworth.arsnouveau.client.gui.utils.RenderUtils;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.muon.medieval.Medieval;
import dev.muon.medieval.hotbar.ArsNouveauManaProvider;
import dev.muon.medieval.hotbar.HUDPositioning;
import dev.muon.medieval.hotbar.ManaBarRenderer;
import dev.muon.medieval.hotbar.Position;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiManaHUD.class)
public class GuiManaHUDMixin {
    @Shadow @Final
    private static Minecraft minecraft;

    @ModifyReturnValue(method = "shouldDisplayBar", at = @At("RETURN"))
    private static boolean fadeOut(boolean original) {
        boolean setToHide = !minecraft.options.hideGui && original;
        ManaBarRenderer.setBarVisibility(setToHide);
        return ManaBarRenderer.isVisible();
    }

    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
    private static void renderCustomManaBar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!GuiManaHUD.shouldDisplayBar()) return;
        if (minecraft.options.hideGui) return;

        Player player = minecraft.player;
        if (player == null) return;

        IManaCap mana = CapabilityRegistry.getMana(player);
        if (mana == null || mana.getMaxMana() == 0) return;

        ManaBarRenderer.render(guiGraphics, deltaTracker, new ArsNouveauManaProvider(mana));
        ci.cancel();
    }
}