package dev.muon.medieval.mixin.compat.ars_nouveau;

import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.client.ClientInfo;
import com.hollingsworth.arsnouveau.client.gui.GuiManaHUD;
import com.hollingsworth.arsnouveau.client.gui.utils.RenderUtils;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.muon.medieval.ManaTextHelper;
import dev.muon.medieval.Medieval;
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
    @Shadow
    @Final
    private static Minecraft minecraft;

    @ModifyReturnValue(method = "shouldDisplayBar", at = @At("RETURN"))
    private static boolean alwaysShowManaBar(boolean original) {
        return !Minecraft.getInstance().options.hideGui;
    }

    @Unique
    private static double lastMana = -1;
    @Unique
    private static long fullManaStartTime = 0;
    @Unique
    private static final long TEXT_DISPLAY_DURATION = 1000L;
    @Unique
    private static final int BASE_TEXT_ALPHA = 200;

    @Unique
    private static int getTextAlpha(long timeSinceFullMana, long timeSinceManaError) {
        int alpha = BASE_TEXT_ALPHA;

        if (timeSinceManaError < TEXT_DISPLAY_DURATION) {
            if (timeSinceManaError > TEXT_DISPLAY_DURATION - 200) {
                alpha = (int)(BASE_TEXT_ALPHA * (TEXT_DISPLAY_DURATION - timeSinceManaError) / 200f);
            }
            return (alpha << 24) | 0xFFFFFF;
        }

        if (fullManaStartTime > 0 && timeSinceFullMana > TEXT_DISPLAY_DURATION - 200) {
            alpha = (int)(BASE_TEXT_ALPHA * (TEXT_DISPLAY_DURATION - timeSinceFullMana) / 200f);
        }

        return (alpha << 24) | 0xFFFFFF;
    }
    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
    private static void renderCustomManaBar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!GuiManaHUD.shouldDisplayBar()) return;

        Player player = minecraft.player;
        if (player == null) return;

        PoseStack ms = guiGraphics.pose();
        IManaCap mana = CapabilityRegistry.getMana(player);
        if (mana == null || mana.getMaxMana() == 0) return;

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        int xPos = (screenWidth / 2) - 97;
        int yPos = screenHeight - 46;

        guiGraphics.blit(
                Medieval.loc("textures/gui/mana_border.png"), xPos, yPos, 0, 0, 108, 18, 256, 256
        );
        float maxMana = mana.getMaxMana() * (1.0f + ClientInfo.reservedOverlayMana);
        double currentMana = mana.getCurrentMana();

        int manaLength = (int) (83 * (currentMana / maxMana));
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

        if (currentMana >= maxMana) {
            if (lastMana < maxMana) {
                fullManaStartTime = System.currentTimeMillis();
            }
        } else {
            fullManaStartTime = 0;
        }
        lastMana = currentMana;

        long timeSinceFullMana = fullManaStartTime > 0 ? System.currentTimeMillis() - fullManaStartTime : 0;
        long timeSinceManaError = System.currentTimeMillis() - ManaTextHelper.getLastManaErrorTime();

        if (currentMana < maxMana ||
                (fullManaStartTime > 0 && timeSinceFullMana < TEXT_DISPLAY_DURATION) ||
                timeSinceManaError < TEXT_DISPLAY_DURATION) {

            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.scale(0.5f, 0.5f, 1.0f);

            String manaText = String.format("%.1f / %.1f",
                    currentMana,
                    maxMana
            ).replaceAll("\\.0", "");

            int textWidth = minecraft.font.width(manaText);
            int textX = (((screenWidth / 2) - 45) - (textWidth / 4)) * 2;
            int textY = (screenHeight - 46) * 2 + 18;

            int color = getTextAlpha(timeSinceFullMana, timeSinceManaError);

            guiGraphics.drawString(minecraft.font, manaText, textX, textY, color, true);
            poseStack.popPose();
        }

        ci.cancel();
    }



}