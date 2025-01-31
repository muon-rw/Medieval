package dev.muon.medieval.hotbar;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.muon.medieval.Medieval;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class StaminaBarRenderer {
    private static final float SPRINT_THRESHOLD = 6.0f;
    //private static final float WARNING_THRESHOLD = 7.0f;

    private enum BarType {
        NORMAL("stamina_bar"),
        HUNGER("stamina_bar_hunger"),
        //WARNING("stamina_bar_warning"),
        CRITICAL("stamina_bar_critical");

        private final String texture;

        BarType(String texture) {
            this.texture = texture;
        }

        public String getTexture() {
            return texture;
        }

        public static BarType fromPlayerState(Player player, float stamina) {
            if (stamina <= SPRINT_THRESHOLD) return CRITICAL;
            if (player.hasEffect(MobEffects.HUNGER)) return HUNGER;
            //if (stamina <= WARNING_THRESHOLD) return WARNING;
            return NORMAL;
        }
    }

    public static void render(GuiGraphics graphics, Player player, DeltaTracker deltaTracker) {
        Position staminaPos = HUDPositioning.getHungerAnchor()
                .offset(HUDPositioning.getStaminaBarXOffset(), HUDPositioning.getStaminaBarYOffset());

        int borderWidth = ConfigConstants.STAMINA_BORDER_WIDTH;
        int borderHeight = ConfigConstants.STAMINA_BORDER_HEIGHT;
        int barWidth = ConfigConstants.STAMINA_BAR_WIDTH;
        int barHeight = ConfigConstants.STAMINA_BAR_HEIGHT;
        int barXOffset = ConfigConstants.STAMINA_BAR_X_OFFSET;
        int barYOffset = ConfigConstants.STAMINA_BAR_Y_OFFSET;
        int animationCycles = ConfigConstants.STAMINA_BAR_ANIMATION_CYCLES;
        int frameHeight = ConfigConstants.STAMINA_BAR_FRAME_HEIGHT;


        int xPos = staminaPos.x() - borderWidth;
        int yPos = staminaPos.y();

        graphics.blit(
                Medieval.loc("textures/gui/stamina_border.png"),
                xPos, yPos, 0, 0, borderWidth, borderHeight, 256, 256
        );

        float maxStamina = 20f;
        float currentStamina = player.getFoodData().getFoodLevel();
        int animOffset = (int) (((player.tickCount + deltaTracker.getGameTimeDeltaTicks()) / 3) % animationCycles) * frameHeight;

        renderBaseBar(graphics, player, currentStamina, maxStamina, xPos, yPos,
                barWidth, barHeight, barXOffset, barYOffset,
                animOffset);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        if (ConfigConstants.STAMINA_DETAIL_OVERLAY) {
            graphics.blit(
                    Medieval.loc("textures/gui/detail_overlay.png"),
                    xPos + ConfigConstants.STAMINA_OVERLAY_X_OFFSET,
                    yPos + ConfigConstants.STAMINA_OVERLAY_Y_OFFSET,
                    0, 0, borderWidth, borderHeight,
                    256, 256
            );
        }
    }

    private static void renderBaseBar(GuiGraphics graphics, Player player, float currentStamina, float maxStamina,
                                      int xPos, int yPos, int barWidth, int barHeight,
                                      int barXOffset, int barYOffset, int animOffset) {
        BarType barType = BarType.fromPlayerState(player, currentStamina);
        int partialBarWidth = (int) (barWidth * (currentStamina / maxStamina));

        graphics.blit(
                Medieval.loc("textures/gui/" + barType.getTexture() + ".png"),
                xPos + barXOffset, yPos + barYOffset,
                0, animOffset, partialBarWidth, barHeight,
                256, 256
        );
    }
}