package dev.muon.medieval.hotbar;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.muon.medieval.Medieval;
import dev.muon.medieval.hotbar.HUDPositioning;
import dev.muon.medieval.hotbar.Position;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import static dev.muon.medieval.hotbar.ConfigConstants.*;
import static dev.muon.medieval.hotbar.RenderUtil.TEXT_DISPLAY_DURATION;

public class HealthBarRenderer {

    private static float lastHealth = -1;
    private static long fullHealthStartTime = 0;

    public static void render(GuiGraphics graphics, Player player, Minecraft minecraft, int height, float maxHealth, float actualHealth, int absorptionAmount, DeltaTracker deltaTracker) {
        Position healthPos = HUDPositioning.getHealthAnchor()
                .offset(HUDPositioning.getHealthBarXOffset(), HUDPositioning.getHealthBarYOffset());

        // Configs
        int borderWidth = HEALTH_BORDER_WIDTH;
        int borderHeight = HEALTH_BORDER_HEIGHT;
        int barWidth = HEALTH_BAR_WIDTH;
        int barHeight = HEALTH_BAR_HEIGHT;
        int barXOffset = HEALTH_BAR_X_OFFSET;
        int barYOffset = HEALTH_BAR_Y_OFFSET;
        int animationCycles = HEALTH_BAR_ANIMATION_CYCLES; // Total frames in animation
        int frameHeight = HEALTH_BAR_FRAME_HEIGHT;      // Height of each frame in texture

        int xPos = healthPos.x();
        int yPos = healthPos.y();

        graphics.blit(
                Medieval.loc("textures/gui/health_border.png"), xPos, yPos, 0, 0, borderWidth, borderHeight, 256, 256
        );

        float healthPercent = actualHealth / maxHealth;

        int partialBarWidth = (int) (barWidth * healthPercent);
        int animOffset = (int) (((player.tickCount + deltaTracker.getGameTimeDeltaTicks()) / 3) % animationCycles) * frameHeight;

        boolean hasAbsorption = absorptionAmount > 1;
        // TODO: Make an overlay for this instead
        if (hasAbsorption) {
            RenderSystem.setShaderColor(0.8f, 1.0f, 0.4f, 1.0f);
        }
        graphics.blit(
                Medieval.loc("textures/gui/health_bar.png"), xPos + barXOffset, yPos + barYOffset, 0, animOffset, partialBarWidth, barHeight, 256, 256
        );
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        int textX = (xPos + (borderWidth / 2));
        int textY = (yPos + barYOffset);

        if (shouldRenderText(actualHealth, maxHealth, hasAbsorption)) {
            int color = getHealthTextColor();
            RenderUtil.renderText(actualHealth + absorptionAmount, maxHealth,
                    graphics, textX, textY, color);
        }

    }

    private static int getHealthTextColor() {
        long timeSinceFullHealth = fullHealthStartTime > 0 ?
                System.currentTimeMillis() - fullHealthStartTime : 0;

        int alpha = RenderUtil.calculateTextAlpha(
                timeSinceFullHealth,
                TEXT_DISPLAY_DURATION,
                RenderUtil.TEXT_FADEOUT_DURATION,
                RenderUtil.BASE_TEXT_ALPHA
        );

        // Values lower than 10 cause rendering artifacts
        alpha = Math.max(10, alpha);

        return (alpha << 24) | 0xFFFFFF;
    }

    private static boolean shouldRenderText(float currentHealth, float maxHealth, boolean hasAbsorption) {
        if (hasAbsorption) {
            return true;
        }
        if (currentHealth >= maxHealth) {
            if (lastHealth < maxHealth) {
                fullHealthStartTime = System.currentTimeMillis();
            }
        } else {
            fullHealthStartTime = 0;
        }
        lastHealth = currentHealth;

        long timeSinceFullHealth = fullHealthStartTime > 0 ? System.currentTimeMillis() - fullHealthStartTime : 0;
        return currentHealth < maxHealth || (fullHealthStartTime > 0 && timeSinceFullHealth < TEXT_DISPLAY_DURATION);
    }
}