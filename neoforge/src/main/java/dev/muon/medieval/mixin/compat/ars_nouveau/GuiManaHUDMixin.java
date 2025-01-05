package dev.muon.medieval.mixin.compat.ars_nouveau;

import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.client.ClientInfo;
import com.hollingsworth.arsnouveau.client.gui.GuiManaHUD;
import com.hollingsworth.arsnouveau.client.gui.utils.RenderUtils;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.muon.medieval.Medieval;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiManaHUD.class)
public class GuiManaHUDMixin {
    @Shadow
    @Final
    private static Minecraft minecraft;

    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
    private static void renderCustomManaBar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!GuiManaHUD.shouldDisplayBar()) return;

        PoseStack ms = guiGraphics.pose();
        IManaCap mana = CapabilityRegistry.getMana(minecraft.player);
        if (mana == null || mana.getMaxMana() == 0) return;

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        int xPos = (screenWidth / 2) - 97;
        int yPos = screenHeight - 46;

        guiGraphics.blit(
                Medieval.loc("textures/gui/mana_border.png"), xPos, yPos, 0, 0, 108, 18, 256, 256
        );

        int manaLength = (int) (83 * (mana.getCurrentMana() / (mana.getMaxMana() * (1.0 + ClientInfo.reservedOverlayMana))));
        int manaOffset = (int) (((ClientInfo.ticksInGame + deltaTracker.getGameTimeDeltaTicks()) / 3 % (33))) * 6;

        guiGraphics.blit(
                Medieval.loc("textures/gui/mana_bar.png"), xPos + 9, yPos + 9, 0, manaOffset, manaLength, 4, 256, 256
        );

        if (ClientInfo.reservedOverlayMana > 0) {
            int reserveManaLength = (int) (96F * ClientInfo.reservedOverlayMana);
            int offset = 96 - reserveManaLength;
            RenderSystem.setShaderTexture(0, Medieval.loc("textures/gui/mana_bar.png"));
            RenderUtils.colorBlit(ms, xPos + 10 + offset, yPos + 8, 0, 0, reserveManaLength, 6, 256, 256, GuiManaHUD.BLACK);
        }

        ci.cancel();
    }

}