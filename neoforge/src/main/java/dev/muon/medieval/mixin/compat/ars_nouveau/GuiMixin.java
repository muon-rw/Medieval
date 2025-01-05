package dev.muon.medieval.mixin.compat.ars_nouveau;

import com.mojang.blaze3d.systems.RenderSystem;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    // TODO: BEFORE RELEASE, REDO BAR ASSETS
    // TODO: Number overlays?
    // TODO: Fix Overflowing Bars compat
    // TODO: Add effects - Absorption, poison, wither, hunger, etc.
    @Shadow @Final private Minecraft minecraft;
    @Unique
    private DeltaTracker currentDeltaTracker;

    @Inject(method = "render", at = @At("HEAD"))
    private void captureDeltaTracker(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        this.currentDeltaTracker = deltaTracker;
    }

    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true, require = 1)
    private void renderCustomHealth(GuiGraphics graphics, Player player, int x, int y, int height, int offsetHeartIndex, float maxHealth, int health, int displayHealth, int absorptionAmount, boolean renderHighlight, CallbackInfo ci) {
        if (minecraft.options.hideGui) return;

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        graphics.blit(
                Medieval.loc("textures/gui/health_border.png"), (screenWidth / 2) - 53, screenHeight - 65, 0, 0, 102, 18, 256, 256
        );

        float healthPercent = (float) health / maxHealth;
        int barLength = (int) (86 * healthPercent);
        int animOffset = (int)(((player.tickCount + currentDeltaTracker.getGameTimeDeltaTicks()) / 3) % 33) * 6;
        graphics.blit(
                Medieval.loc("textures/gui/health_bar.png"), (screenWidth / 2) - 44, screenHeight - 56, 0, animOffset, barLength, 4, 256, 256
        );

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

}