package dev.muon.medieval.hotbar;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.muon.medieval.Medieval;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

public class StaminaBarRenderer {
    private static float lastStamina = -1;
    private static long aboveThresholdStartTime = 0;
    private static final float SPRINT_THRESHOLD = 6.0f;  // Gameplay-relevant threshold

    public static void render(GuiGraphics graphics, Player player, DeltaTracker deltaTracker) {

        FoodData foodData = player.getFoodData();
        int foodLevel = foodData.getFoodLevel();

        Position staminaPos = HUDPositioning.getHungerAnchor()
                .offset(HUDPositioning.getStaminaBarXOffset(), HUDPositioning.getStaminaBarYOffset());

        // Configs
        int borderWidth = 80;
        int borderHeight = 10;
        int barWidth = 74;
        int barHeight = 4;
        int barXOffset = 3;
        int barYOffset = 3;
        int animationCycles = 33; // Total frames in animation
        int frameHeight = 6;      // Height of each frame in texture

        int xPos = staminaPos.x() - borderWidth; // subtract full width to align-right
        int yPos = staminaPos.y();

        graphics.blit(
                Medieval.loc("textures/gui/stamina_border.png"),
                xPos, yPos, 0, 0, borderWidth, borderHeight, 256, 256
        );

        float maxStamina = 20f;
        float currentStamina = player.getFoodData().getFoodLevel();
        float staminaPercent = currentStamina / maxStamina;
        int partialBarWidth = (int) (barWidth * staminaPercent);

        int animOffset = (int) (((player.tickCount + deltaTracker.getGameTimeDeltaTicks()) / 3) % animationCycles) * frameHeight;


        boolean hasHungerEffect = player.hasEffect(MobEffects.HUNGER);
        if (hasHungerEffect) {
            graphics.blit(
                    Medieval.loc("textures/gui/stamina_bar_hunger.png"),
                    xPos + barXOffset, yPos + barYOffset,
                    0, animOffset, partialBarWidth, barHeight, 256, 256
            );
        } else {
            graphics.blit(
                    Medieval.loc("textures/gui/stamina_bar.png"),
                    xPos + barXOffset, yPos + barYOffset,
                    0, animOffset, partialBarWidth, barHeight, 256, 256
            );
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        int textX = (xPos + (borderWidth / 2));
        int textY = (yPos + barYOffset);

        if (shouldRenderText(currentStamina, maxStamina, hasHungerEffect)) {
            int color = getStaminaTextColor();
            RenderUtil.renderText(currentStamina, maxStamina, graphics, textX, textY, color);
        }
    }

    private static boolean shouldRenderText(float currentStamina, float maxStamina, boolean hasHungerEffect) {
        if (hasHungerEffect) {
            return true;
        }

        if (currentStamina >= SPRINT_THRESHOLD) {
            if (lastStamina < SPRINT_THRESHOLD) {
                aboveThresholdStartTime = System.currentTimeMillis();
            }
        } else {
            aboveThresholdStartTime = 0;
        }
        lastStamina = currentStamina;

        // Show text if:
        // 1. Currently below sprint threshold OR
        // 2. Just transitioned above threshold (with fade-out)
        long timeSinceAboveThreshold = aboveThresholdStartTime > 0 ?
                System.currentTimeMillis() - aboveThresholdStartTime : 0;

        return currentStamina < SPRINT_THRESHOLD ||
                (aboveThresholdStartTime > 0 && timeSinceAboveThreshold < RenderUtil.TEXT_DISPLAY_DURATION);
    }

    private static int getStaminaTextColor() {
        long timeSinceAboveThreshold = aboveThresholdStartTime > 0 ?
                System.currentTimeMillis() - aboveThresholdStartTime : 0;

        int alpha = RenderUtil.calculateTextAlpha(
                timeSinceAboveThreshold,
                RenderUtil.TEXT_DISPLAY_DURATION,
                RenderUtil.TEXT_FADEOUT_DURATION,
                RenderUtil.BASE_TEXT_ALPHA
        );
        return (alpha << 24) | 0xFFFFFF;
    }
}