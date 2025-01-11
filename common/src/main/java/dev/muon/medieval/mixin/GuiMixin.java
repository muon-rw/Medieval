package dev.muon.medieval.mixin;

import dev.muon.medieval.hotbar.ConfigConstants;
import dev.muon.medieval.hotbar.HealthBarRenderer;
import dev.muon.medieval.hotbar.StaminaBarRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
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
    @Shadow @Final
    private Minecraft minecraft;

    @Unique
    private DeltaTracker medieval$currentDeltaTracker;

    @Inject(method = "render", at = @At("HEAD"))
    private void captureDeltaTracker(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        this.medieval$currentDeltaTracker = deltaTracker;
    }


    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    private void renderCustomHealth(GuiGraphics graphics, Player player, int originalX, int originalY, int height, int offsetHeartIndex, float maxHealth, int health, int displayHealth, int absorptionAmount, boolean renderHighlight, CallbackInfo ci) {
        if (minecraft.options.hideGui) return;

        HealthBarRenderer.render(graphics, player,
                player.getMaxHealth(), player.getHealth(),
                absorptionAmount, medieval$currentDeltaTracker);

        ci.cancel();
    }


    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void renderCustomFood(GuiGraphics graphics, Player player, int originalY, int originalX, CallbackInfo ci) {
        if (minecraft.options.hideGui) return;

        StaminaBarRenderer.render(graphics, player, medieval$currentDeltaTracker);

        ci.cancel();
    }


    @ModifyArg(
            method = "renderArmor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"),
            index = 2
    )
    private static int cancelHealthBasedShifting(int originalY) {
        if (ConfigConstants.ENABLE_CUSTOM_HEALTH) {
            return Minecraft.getInstance().getWindow().getGuiScaledHeight() - 49;
        }
        return originalY;
    }
}