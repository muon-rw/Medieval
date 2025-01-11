package dev.muon.medieval.hotbar;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.muon.medieval.Medieval;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;

import static dev.muon.medieval.hotbar.ConfigConstants.*;
import static dev.muon.medieval.hotbar.RenderUtil.TEXT_DISPLAY_DURATION;

public class HealthBarRenderer {

    private static float lastHealth = -1;
    private static long fullHealthStartTime = 0;

    private enum BarType {
        NORMAL("health_bar"),
        POISON("health_bar_poison"),
        WITHER("health_bar_wither"),
        FROZEN("health_bar_frozen"),
        SCORCHED("health_bar_scorched");

        private final String texture;

        BarType(String texture) {
            this.texture = texture;
        }

        public String getTexture() {
            return texture;
        }

        public static BarType fromPlayerState(Player player) {
            if (player.hasEffect(MobEffects.POISON)) return POISON;
            if (player.hasEffect(MobEffects.WITHER)) return WITHER;
            if (isFrozen(player)) return FROZEN;
            if (isScorched(player)) return SCORCHED;
            return NORMAL;
        }
    }

    public static void render(GuiGraphics graphics, Player player, float maxHealth, float actualHealth, int absorptionAmount, DeltaTracker deltaTracker) {
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

        int animOffset = (int) (((player.tickCount + deltaTracker.getGameTimeDeltaTicks()) / 3) % animationCycles) * frameHeight;

        boolean hasAbsorption = absorptionAmount > 1;

        renderBaseBar(graphics, player, maxHealth, actualHealth, xPos, yPos, barWidth, barHeight, barXOffset, barYOffset, animOffset);
        renderOverlays(graphics, player, absorptionAmount, xPos, yPos, barWidth, barHeight, barXOffset, barYOffset);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);


        int textX = (xPos + (borderWidth / 2));
        int textY = (yPos + barYOffset);

        if (shouldRenderText(actualHealth, maxHealth, hasAbsorption)) {
            int color = getHealthTextColor();
            RenderUtil.renderText(actualHealth + absorptionAmount, maxHealth,
                    graphics, textX, textY, color);
        }
    }

    private static void renderBaseBar(GuiGraphics graphics, Player player, float maxHealth, float actualHealth,
                                      int xPos, int yPos, int barWidth, int barHeight,
                                      int barXOffset, int barYOffset, int animOffset) {
        BarType barType = BarType.fromPlayerState(player);
        int partialBarWidth = (int) (barWidth * (actualHealth / maxHealth));

        graphics.blit(
                Medieval.loc("textures/gui/" + barType.getTexture() + ".png"),
                xPos + barXOffset, yPos + barYOffset,
                0, animOffset, partialBarWidth, barHeight,
                256, 256
        );
    }

    private static void renderOverlays(GuiGraphics graphics, Player player, int absorptionAmount,
                                       int xPos, int yPos, int barWidth, int barHeight,
                                       int barXOffset, int barYOffset) {

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        float tempScale = getTemperatureScale(player);
        renderTemperatureOverlay(graphics, tempScale, xPos, yPos, barWidth, barHeight, barXOffset, barYOffset);
        // TODO: Scorchful wetness overlay
        // TODO: Improve overlay sprites

        if (absorptionAmount > 0) {
            graphics.blit(
                    Medieval.loc("textures/gui/absorption_overlay.png"),
                    xPos + barXOffset, yPos + barYOffset,
                    0, 0, barWidth, barHeight,
                    256, 256
            );
        }
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void renderTemperatureOverlay(GuiGraphics graphics, float tempScale,
                                                 int xPos, int yPos, int barWidth, int barHeight,
                                                 int barXOffset, int barYOffset) {
        if (tempScale > 0) {
            int heatWidth = (int) (barWidth * tempScale);
            graphics.blit(
                    Medieval.loc("textures/gui/heat_overlay.png"),
                    xPos + barXOffset, yPos + barYOffset,
                    0, 0, heatWidth, barHeight,
                    256, 256
            );
        } else if (tempScale < 0) {
            int coldWidth = (int) (barWidth * -tempScale);
            graphics.blit(
                    Medieval.loc("textures/gui/cold_overlay.png"),
                    xPos + barXOffset, yPos + barYOffset,
                    0, 0, coldWidth, barHeight,
                    256, 256
            );
        }
    }

    // TODO: Move to something less cursed / easier to extend
    private static boolean isScorched(Player player) {
        try {
            Method getMaxTemp = player.getClass().getMethod("thermoo$getMaxTemperature");
            Method getTemp = player.getClass().getMethod("thermoo$getTemperature");

            int maxTemperature = (int) getMaxTemp.invoke(player);
            int temperature = (int) getTemp.invoke(player);

            return temperature >= maxTemperature - 1;
        } catch (Exception e) {
            // Thermoo not present
            return false;
        }
    }

    private static boolean isFrozen(Player player) {
        if (player.isFullyFrozen()) {
            return true;
        }

        // TODO: Move to something less cursed / easier to extend
        try {
            Method getTemp = player.getClass().getMethod("thermoo$getTemperature");
            Method getTempScale = player.getClass().getMethod("thermoo$getTemperatureScale");

            int minTemperature = (int) getTemp.invoke(player);
            if (minTemperature < 0) {
                float tempScale = (float) getTempScale.invoke(player);
                return tempScale <= -0.99f;
            }
        } catch (Exception e) {
            // Revert to vanilla check
        }

        return false;
    }

    // TODO: Move to something less cursed / easier to extend
    private static float getTemperatureScale(Player player) {
        try {
            Method getTempScale = player.getClass().getMethod("thermoo$getTemperatureScale");
            return (float) getTempScale.invoke(player);
        } catch (Exception e) {
            return 0.0f;
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