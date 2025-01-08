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
import dev.muon.medieval.client.HUDPositioning;
import dev.muon.medieval.client.Position;
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
    @Unique
    private static float lastMana = -1;
    @Unique
    private static long fullManaStartTime = 0;
    @Unique
    private static final int TEXT_ALPHA = 200;
    @Unique
    private static final long BAR_FADE_DURATION = 1000L;
    @Unique
    private static final long TEXT_DISPLAY_DURATION = 2000L;
    @Unique
    private static final long TEXT_FADEOUT_DURATION = 1000L;

    @Unique
    private static long barEnabledStartTime = 0L;
    @Unique
    private static long barDisabledStartTime = 0L;
    @Unique
    private static boolean barSetVisible = false;


    @ModifyReturnValue(method = "shouldDisplayBar", at = @At("RETURN"))
    private static boolean fadeOut(boolean original) {
        boolean setToHide = !minecraft.options.hideGui && original;

        if (barSetVisible != setToHide) {
            if (setToHide) {
                barEnabledStartTime = System.currentTimeMillis();
            }
            if (!setToHide) {
                barDisabledStartTime = System.currentTimeMillis();
            }
            barSetVisible = setToHide;
        }

        if (!setToHide && System.currentTimeMillis() - barDisabledStartTime > BAR_FADE_DURATION) {
            return false;
        }
        return true;
    }

    @Unique
    private static float getCurrentAlpha() {
        if (barSetVisible) return 1.0f;

        long timeSinceHide = System.currentTimeMillis() - GuiManaHUDMixin.barDisabledStartTime;
        return Math.max(0, 1 - (timeSinceHide / (float) BAR_FADE_DURATION));
    }


    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
    private static void renderCustomManaBar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!GuiManaHUD.shouldDisplayBar()) return;
        if (minecraft.options.hideGui) return;

        Player player = minecraft.player;
        if (player == null) return;

        IManaCap mana = CapabilityRegistry.getMana(player);
        if (mana == null || mana.getMaxMana() == 0) return;

        renderMana(guiGraphics, deltaTracker, mana);

        ci.cancel();
    }

    @Unique
    private static void renderMana(GuiGraphics guiGraphics, DeltaTracker deltaTracker, IManaCap mana) {
        float alpha = getCurrentAlpha();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

        Position manaPos = HUDPositioning.getAboveUtilitiesAnchor()
                .offset(HUDPositioning.getManaBarXOffset(), HUDPositioning.getManaBarYOffset());

        // Configs
        int borderWidth = 80;
        int borderHeight = 10;
        int overlayWidth = 80;
        int overlayHeight = 10;
        int barWidth = 74;
        int barHeight = 4;
        int barXOffset = 3;
        int barYOffset = 3;
        int animationCycles = 33; // Total frames in animation
        int frameHeight = 6;      // Height of each frame in texture
        int overlayXOffest = 0;
        int overlayYOffset = -3;


        int xPos = manaPos.x() - (borderWidth / 2); // subtract half-width to align-center
        int yPos = manaPos.y();

        guiGraphics.blit(
                Medieval.loc("textures/gui/mana_border.png"),
                xPos, yPos, 0, 0, borderWidth, borderHeight, 256, 256
        );


        float maxMana = mana.getMaxMana() * (1.0f + ClientInfo.reservedOverlayMana);
        double currentMana = mana.getCurrentMana();
        int partialBarWidth = (int) (barWidth * (currentMana / maxMana));

        int manaOffset = (int) (((ClientInfo.ticksInGame + deltaTracker.getGameTimeDeltaTicks()) / 3 % animationCycles)) * frameHeight;

        guiGraphics.blit(
                Medieval.loc("textures/gui/mana_bar.png"),
                xPos + barXOffset, yPos + barYOffset,
                0, manaOffset, partialBarWidth, barHeight, 256, 256
        );

        renderReservedMana(guiGraphics.pose(), xPos, yPos, barWidth, barHeight, barXOffset, barYOffset);

        guiGraphics.blit(
                Medieval.loc("textures/gui/detail_overlay.png"),
                xPos + overlayXOffest, yPos + overlayYOffset, 0, 0, overlayWidth, overlayHeight, 256, 256
        );

        Position textPosition = new Position(xPos + (borderWidth / 2), yPos + barYOffset);
        renderManaText(guiGraphics, currentMana, maxMana, textPosition);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Unique
    private static void renderReservedMana(PoseStack ms, int xPos, int yPos, int barWidth, int barHeight, int barXOffset, int barYOffset) {
        if (ClientInfo.reservedOverlayMana > 0) {
            int reserveManaLength = (int) (barWidth * ClientInfo.reservedOverlayMana);
            int offset = barWidth - reserveManaLength;
            RenderSystem.setShaderTexture(0, Medieval.loc("textures/gui/mana_bar.png"));
            RenderUtils.colorBlit(ms, xPos + barXOffset + offset, yPos + barYOffset,
                    0, 0, reserveManaLength, barHeight, 256, 256, GuiManaHUD.BLACK);
        }
    }

    @Unique
    private static boolean shouldDisplayManaText(double currentMana, float maxMana) {
        long timeSinceBarVisible = System.currentTimeMillis() - barEnabledStartTime;
        long timeSinceFullMana = fullManaStartTime > 0 ? System.currentTimeMillis() - fullManaStartTime : 0;

        if (barSetVisible && timeSinceBarVisible < TEXT_DISPLAY_DURATION) {
            return true;
        }

        if (currentMana < maxMana) {
            return true;
        }

        if (currentMana >= maxMana && lastMana < maxMana) {
            fullManaStartTime = System.currentTimeMillis();
        }

        lastMana = (float) currentMana;
        return false;
    }

    @Unique
    private static int getManaTextAlpha(long timeSinceFullMana) {
        long timeSinceBarVisible = System.currentTimeMillis() - barEnabledStartTime;
        int alpha;

        if (barSetVisible && timeSinceBarVisible < TEXT_DISPLAY_DURATION) {
            // Bar visibility fade
            alpha = timeSinceBarVisible > TEXT_DISPLAY_DURATION - TEXT_FADEOUT_DURATION
                    ? (int)(TEXT_ALPHA * (TEXT_DISPLAY_DURATION - timeSinceBarVisible) / TEXT_FADEOUT_DURATION)
                    : TEXT_ALPHA;
        } else if (timeSinceFullMana < TEXT_DISPLAY_DURATION) {
            // Full mana transition fade
            alpha = timeSinceFullMana > TEXT_DISPLAY_DURATION - TEXT_FADEOUT_DURATION
                    ? (int)(TEXT_ALPHA * (TEXT_DISPLAY_DURATION - timeSinceFullMana) / TEXT_FADEOUT_DURATION)
                    : TEXT_ALPHA;
        } else {
            alpha = 0;
        }

        // Clamp alpha between 10 and TEXT_ALPHA to prevent rendering artifacts
        alpha = Math.max(10, Math.min(alpha, TEXT_ALPHA));

        return (alpha << 24) | 0xFFFFFF;
    }

    @Unique
    private static void renderManaText(GuiGraphics graphics, double currentMana, float maxMana, Position barPosition) {
        if (!shouldDisplayManaText(currentMana, maxMana)) {
            return;
        }

        int textX = barPosition.x() * 2;
        int textY = barPosition.y() * 2;

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 1.0f);

        String currentText = String.valueOf((int) currentMana);
        String maxText = String.valueOf((int) maxMana);
        String slashText = "/";

        int slashWidth = minecraft.font.width(slashText);
        int currentWidth = minecraft.font.width(currentText);

        int slashX = textX - (slashWidth / 2);
        long timeSinceFullMana = fullManaStartTime > 0 ? System.currentTimeMillis() - fullManaStartTime : 0;
        int color = getManaTextAlpha(timeSinceFullMana);

        graphics.drawString(minecraft.font, currentText, slashX - currentWidth, textY, color, true);
        graphics.drawString(minecraft.font, slashText, slashX, textY, color, true);
        graphics.drawString(minecraft.font, maxText, slashX + slashWidth, textY, color, true);

        poseStack.popPose();
    }

}