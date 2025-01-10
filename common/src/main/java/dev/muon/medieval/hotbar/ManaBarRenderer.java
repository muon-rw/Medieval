package dev.muon.medieval.hotbar;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.muon.medieval.Medieval;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public class ManaBarRenderer {
    private static float lastMana = -1;
    private static long fullManaStartTime = 0;
    private static long barEnabledStartTime = 0L;
    private static long barDisabledStartTime = 0L;
    private static boolean barSetVisible = false;

    private static final int RESERVED_MANA_COLOR = 0x232323;


    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker, ManaProvider manaProvider) {

        float alpha = getCurrentAlpha();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

        Position manaPos = HUDPositioning.getAboveUtilitiesAnchor()
                .offset(HUDPositioning.getManaBarXOffset(), HUDPositioning.getManaBarYOffset());

        // Configs from constants
        int borderWidth = ConfigConstants.MANA_BORDER_WIDTH;
        int borderHeight = ConfigConstants.MANA_BORDER_HEIGHT;
        int barWidth = ConfigConstants.MANA_BAR_WIDTH;
        int barHeight = ConfigConstants.MANA_BAR_HEIGHT;
        int barXOffset = ConfigConstants.MANA_BAR_X_OFFSET;
        int barYOffset = ConfigConstants.MANA_BAR_Y_OFFSET;
        int animationCycles = ConfigConstants.MANA_BAR_ANIMATION_CYCLES;
        int frameHeight = ConfigConstants.MANA_BAR_FRAME_HEIGHT;

        int xPos = manaPos.x() - (borderWidth / 2);
        int yPos = manaPos.y();

        renderMainBar(graphics, manaProvider, deltaTracker, xPos, yPos,
                borderWidth, borderHeight, barWidth, barHeight,
                barXOffset, barYOffset, animationCycles, frameHeight);

        renderReservedOverlay(graphics, manaProvider, deltaTracker,
                xPos, yPos, barWidth, barHeight,
                barXOffset, barYOffset, animationCycles, frameHeight);

        if (ConfigConstants.MANA_DETAIL_OVERLAY) {
            graphics.blit(Medieval.loc("textures/gui/detail_overlay.png"),
                    xPos + ConfigConstants.MANA_OVERLAY_X_OFFSET,
                    yPos + ConfigConstants.MANA_OVERLAY_Y_OFFSET,
                    0, 0, borderWidth, borderHeight, 256, 256);
        }


        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        int textX = (xPos + (borderWidth / 2));
        int textY = (yPos + barYOffset);
        if (shouldRenderText(manaProvider.getCurrentMana(), manaProvider.getMaxMana())) {
            int color = getManaTextColor();
            RenderUtil.renderText((float) manaProvider.getCurrentMana(), manaProvider.getMaxMana(),
                    graphics, textX, textY, color);
        }
    }

    private static int getManaTextColor() {
        long timeSinceFullMana = fullManaStartTime > 0 ?
                System.currentTimeMillis() - fullManaStartTime : 0;

        int alpha = RenderUtil.calculateTextAlpha(
                timeSinceFullMana,
                RenderUtil.TEXT_DISPLAY_DURATION,
                RenderUtil.TEXT_FADEOUT_DURATION,
                RenderUtil.BASE_TEXT_ALPHA
        );

        if (!barSetVisible) {
            float barAlpha = getCurrentAlpha();
            alpha = (int)(alpha * barAlpha);
        }
        alpha = Math.max(10, alpha);

        return (alpha << 24) | 0xFFFFFF;
    }



    private static boolean shouldRenderText(double currentMana, float maxMana) {
        if (currentMana >= maxMana) {
            if (lastMana < maxMana) {
                fullManaStartTime = System.currentTimeMillis();
            }
        } else {
            fullManaStartTime = 0;
        }
        lastMana = (float) currentMana;

        long timeSinceFullMana = fullManaStartTime > 0 ?
                System.currentTimeMillis() - fullManaStartTime : 0;

        // Stop rendering before alpha would drop below 10
        return (currentMana < maxMana ||
                (fullManaStartTime > 0 && timeSinceFullMana < RenderUtil.TEXT_DISPLAY_DURATION))
                && getCurrentAlpha() > 0.05f;
    }

    private static float getCurrentAlpha() {
        if (barSetVisible) return 1.0f;
        long timeSinceHide = System.currentTimeMillis() - barDisabledStartTime;
        return Math.max(0, 1 - (timeSinceHide / (float) RenderUtil.BAR_FADEOUT_DURATION));
    }


    public static void setBarVisibility(boolean visible) {
        if (barSetVisible != visible) {
            if (visible) {
                barEnabledStartTime = System.currentTimeMillis();
            } else {
                barDisabledStartTime = System.currentTimeMillis();
            }
            barSetVisible = visible;
        }
    }

    private static void renderMainBar(GuiGraphics graphics, ManaProvider manaProvider,
                                      DeltaTracker deltaTracker, int xPos, int yPos,
                                      int borderWidth, int borderHeight, int barWidth, int barHeight,
                                      int barXOffset, int barYOffset, int animationCycles, int frameHeight) {
        // Render border
        graphics.blit(Medieval.loc("textures/gui/mana_border.png"),
                xPos, yPos, 0, 0, borderWidth, borderHeight, 256, 256);

        // Render mana bar
        float maxMana = manaProvider.getMaxMana() * (1.0f + manaProvider.getReservedMana());
        double currentMana = manaProvider.getCurrentMana();
        int partialBarWidth = (int) (barWidth * (currentMana / maxMana));
        int manaOffset = (int) (((manaProvider.getGameTime() + deltaTracker.getGameTimeDeltaTicks()) / 3 % animationCycles)) * frameHeight;

        graphics.blit(Medieval.loc("textures/gui/mana_bar.png"),
                xPos + barXOffset, yPos + barYOffset,
                0, manaOffset, partialBarWidth, barHeight, 256, 256);
    }

    private static void renderReservedOverlay(GuiGraphics graphics, ManaProvider manaProvider,
                                              DeltaTracker deltaTracker, int xPos, int yPos,
                                              int barWidth, int barHeight, int barXOffset, int barYOffset,
                                              int animationCycles, int frameHeight) {
        float reservedMana = manaProvider.getReservedMana();
        if (reservedMana <= 0) return;

        int reserveManaLength = (int) (barWidth * reservedMana);
        int offset = barWidth - reserveManaLength;
        int manaOffset = (int) (((manaProvider.getGameTime() + deltaTracker.getGameTimeDeltaTicks()) / 3 % animationCycles)) * frameHeight;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(((RESERVED_MANA_COLOR >> 16) & 0xFF) / 255f,
                ((RESERVED_MANA_COLOR >> 8) & 0xFF) / 255f,
                (RESERVED_MANA_COLOR & 0xFF) / 255f,
                1.0f);

        graphics.blit(
                Medieval.loc("textures/gui/mana_bar.png"), xPos + barXOffset + offset, yPos + barYOffset,
                0, manaOffset, reserveManaLength, barHeight, 256, 256
        );

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }


    public static boolean isVisible() {
        if (!barSetVisible && System.currentTimeMillis() - barDisabledStartTime > RenderUtil.BAR_FADEOUT_DURATION) {
            return false;
        }
        return true;
    }

}