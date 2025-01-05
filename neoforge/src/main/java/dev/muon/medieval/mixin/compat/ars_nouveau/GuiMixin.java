package dev.muon.medieval.mixin.compat.ars_nouveau;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.muon.medieval.Medieval;
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
    @Shadow @Final private Minecraft minecraft;
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
    private static final long TEXT_DISPLAY_DURATION = 1000L; // 1 second; ms
    @Unique
    private static final int BASE_TEXT_ALPHA = 200;

    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    private void renderCustomHealth(GuiGraphics graphics, Player player, int x, int y, int height, int offsetHeartIndex, float maxHealth, int health, int displayHealth, int absorptionAmount, boolean renderHighlight, CallbackInfo ci) {
        if (minecraft.options.hideGui) return;

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        graphics.blit(
                Medieval.loc("textures/gui/health_border.png"), (screenWidth / 2) - 53, screenHeight - 65, 0, 0, 102, 18, 256, 256
        );

        float actualCurrentHealth = player.getHealth();
        float healthPercent = actualCurrentHealth / maxHealth;

        int barLength = (int)(86 * healthPercent);
        int animOffset = (int)(((player.tickCount + currentDeltaTracker.getGameTimeDeltaTicks()) / 3) % 33) * 6;
        graphics.blit(
                Medieval.loc("textures/gui/health_bar.png"), (screenWidth / 2) - 44, screenHeight - 56, 0, animOffset, barLength, 4, 256, 256
        );

        if (actualCurrentHealth >= maxHealth) {
            if (lastHealth < maxHealth) {
                fullHealthStartTime = System.currentTimeMillis();
            }
        } else {
            fullHealthStartTime = 0;
        }
        lastHealth = actualCurrentHealth;

        long timeSinceFullHealth = fullHealthStartTime > 0 ? System.currentTimeMillis() - fullHealthStartTime : 0;
        if (actualCurrentHealth < maxHealth || (fullHealthStartTime > 0 && timeSinceFullHealth < TEXT_DISPLAY_DURATION)) {
            PoseStack poseStack = graphics.pose();
            poseStack.pushPose();
            poseStack.scale(0.5f, 0.5f, 1.0f);

            String healthText = String.format("%.1f / %.1f",
                    actualCurrentHealth,
                    maxHealth
            ).replaceAll("\\.0", "");

            int textWidth = minecraft.font.width(healthText);
            int textX = ((screenWidth / 2) - (textWidth / 4)) * 2;
            int textY = (screenHeight - 56) * 2;

            int alpha = BASE_TEXT_ALPHA;
            if (fullHealthStartTime > 0 && timeSinceFullHealth > TEXT_DISPLAY_DURATION - 200) {
                alpha = (int)(BASE_TEXT_ALPHA * (TEXT_DISPLAY_DURATION - timeSinceFullHealth) / 200f);
            }
            int color = (alpha << 24) | 0xFFFFFF;

            graphics.drawString(minecraft.font, healthText, textX, textY, color, true);
            poseStack.popPose();
        }

        ci.cancel();
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void renderCustomFood(GuiGraphics graphics, Player player, int y, int x, CallbackInfo ci) {
        if (minecraft.options.hideGui) return;

        FoodData foodData = player.getFoodData();
        int foodLevel = foodData.getFoodLevel();

        int xPos = x - 96;
        int yPos = y - 7;

        graphics.blit(
                Medieval.loc("textures/gui/stamina_border.png"), xPos, yPos, 0, 0, 104, 18, 256, 256
        );

        float foodPercent = foodLevel / 20f;
        int barLength = (int) (84 * foodPercent);

        int animOffset = (int)(((player.tickCount + currentDeltaTracker.getGameTimeDeltaTicks()) / 3) % 33) * 6;

        if (player.hasEffect(MobEffects.HUNGER)) {
            RenderSystem.setShaderColor(0.4f, 0.8f, 0.4f, 1.0f);
        }
        graphics.blit(
                Medieval.loc("textures/gui/stamina_bar.png"), xPos + 9, yPos + 9, 0, animOffset, barLength, 4, 256, 256
        );

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        ci.cancel();
    }

    @ModifyArg(
            method = "renderArmor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"),
            index = 2
    )
    private static int modifyArmorY(int originalY) {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight() - 49;
    }
}