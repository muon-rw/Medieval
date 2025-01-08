package dev.muon.medieval.mixin.compat.ars_nouveau;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.muon.medieval.Medieval;
import dev.muon.medieval.client.HUDPositioning;
import dev.muon.medieval.client.Position;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    // TODO: BEFORE RELEASE, REDO BAR ASSETS
    // TODO: Add effects - Absorption, poison, wither, hunger, etc.
    @Shadow
    @Final
    private Minecraft minecraft;
    @Unique
    private DeltaTracker currentDeltaTracker;

    @Inject(method = "render", at = @At("HEAD"))
    private void captureDeltaTracker(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        this.currentDeltaTracker = deltaTracker;
    }

    @Unique
    private float lastHealth = -1;
    @Unique
    private long fullHealthStartTime = 0;
    @Unique
    private static final long TEXT_DISPLAY_DURATION = 2000L; // 1 second; ms
    @Unique
    private static final long TEXT_FADEOUT_DURATION = 1000L;
    @Unique
    private static final int BASE_TEXT_ALPHA = 200;

    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    private void renderCustomHealth(GuiGraphics graphics, Player player, int originalX, int originalY, int height, int offsetHeartIndex, float maxHealth, int health, int displayHealth, int absorptionAmount, boolean renderHighlight, CallbackInfo ci) {
        if (minecraft.options.hideGui) return;

        Position healthPos = HUDPositioning.getHealthAnchor()
                .offset(HUDPositioning.getHealthBarXOffset(), HUDPositioning.getHealthBarYOffset());

        // Configs
        int borderWidth = 80;
        int borderHeight = 10;
        int barWidth = 74;
        int barHeight = 4;
        int barXOffset = 3;
        int barYOffset = 3;
        int animationCycles = 33; // Total frames in animation
        int frameHeight = 6;      // Height of each frame in texture

        int xPos = healthPos.x();
        int yPos = healthPos.y();

        graphics.blit(
                Medieval.loc("textures/gui/health_border.png"), xPos, yPos, 0, 0, borderWidth, borderHeight, 256, 256
        );

        float actualCurrentHealth = player.getHealth();
        float healthPercent = actualCurrentHealth / maxHealth;

        int partialBarWidth = (int) (barWidth * healthPercent);
        int animOffset = (int) (((player.tickCount + currentDeltaTracker.getGameTimeDeltaTicks()) / 3) % animationCycles) * frameHeight;
        graphics.blit(
                Medieval.loc("textures/gui/health_bar.png"), xPos + barXOffset, yPos + barYOffset, 0, animOffset, partialBarWidth, barHeight, 256, 256
        );

        int textX = (xPos + (borderWidth / 2)) * 2;
        int textY = (yPos + barYOffset) * 2;

        boolean hasAbsorption = absorptionAmount > 1;
        if (shouldRenderHealthText(actualCurrentHealth, maxHealth, hasAbsorption)) {
            renderText(actualCurrentHealth + absorptionAmount, maxHealth, graphics, textX, textY);
        }

        ci.cancel();
    }

    @Unique
    private boolean shouldRenderHealthText(float currentHealth, float maxHealth, boolean hasAbsorption) {
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

    @Unique
    private static int getTextAlpha(long timeSinceFullHealth) {
        int alpha;

        if (timeSinceFullHealth < TEXT_DISPLAY_DURATION) {
            alpha = timeSinceFullHealth > TEXT_DISPLAY_DURATION - TEXT_FADEOUT_DURATION
                    ? (int)(BASE_TEXT_ALPHA * (TEXT_DISPLAY_DURATION - timeSinceFullHealth) / TEXT_FADEOUT_DURATION)
                    : BASE_TEXT_ALPHA;
        } else {
            alpha = 0;
        }

        // Clamp alpha between 10 and BASE_TEXT_ALPHA to prevent rendering artifacts
        alpha = Math.max(10, Math.min(alpha, BASE_TEXT_ALPHA));

        return (alpha << 24) | 0xFFFFFF;
    }


    @Unique
    private void renderText(float current, float max, GuiGraphics graphics, int xPos, int yPos) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 1.0f);

        String currentText = String.valueOf((int)current); // TODO: maybe add a user configurable option to allow floats here
        String maxText = String.valueOf((int)max);
        String slashText = "/"; // TODO: User formatting option

        int slashWidth = minecraft.font.width(slashText);
        int currentWidth = minecraft.font.width(currentText);

        int slashX = xPos - (slashWidth / 2);

        long timeSinceFullHealth = fullHealthStartTime > 0 ? System.currentTimeMillis() - fullHealthStartTime : 0;
        int color = getTextAlpha(timeSinceFullHealth);

        graphics.drawString(minecraft.font, currentText, slashX - currentWidth, yPos, color, true);
        graphics.drawString(minecraft.font, slashText, slashX, yPos, color, true);
        graphics.drawString(minecraft.font, maxText, slashX + slashWidth, yPos, color, true);

        poseStack.popPose();
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void renderCustomFood(GuiGraphics graphics, Player player, int originalY, int originalX, CallbackInfo ci) {
        if (minecraft.options.hideGui) return;

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
                Medieval.loc("textures/gui/stamina_border.png"), xPos, yPos, 0, 0, borderWidth, borderHeight, 256, 256
        );

        float foodPercent = foodLevel / 20f;
        int partialBarWidth = (int) (barWidth * foodPercent);

        int animOffset = (int) (((player.tickCount + currentDeltaTracker.getGameTimeDeltaTicks()) / 3) % animationCycles) * frameHeight;

        if (player.hasEffect(MobEffects.HUNGER)) {
            RenderSystem.setShaderColor(0.4f, 0.8f, 0.4f, 1.0f);
        }
        graphics.blit(
                Medieval.loc("textures/gui/stamina_bar.png"), xPos + barXOffset, yPos + barYOffset, 0, animOffset, partialBarWidth, barHeight, 256, 256
        );

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        ci.cancel();
    }


    @ModifyArg(
            method = "renderArmor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"),
            index = 2
    )
    private static int cancelHealthBasedShifting(int originalY) {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight() - 49;
    }
}