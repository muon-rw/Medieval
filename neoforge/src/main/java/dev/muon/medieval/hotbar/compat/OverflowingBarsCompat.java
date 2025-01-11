package dev.muon.medieval.hotbar.compat;

import dev.muon.medieval.hotbar.ConfigConstants;
import dev.muon.medieval.hotbar.HealthBarRenderer;
import fuzs.puzzleslib.api.client.core.v1.ClientAbstractions;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public class OverflowingBarsCompat {
    public static EventResult onRenderPlayerHealth(Minecraft minecraft, GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui) {
            return EventResult.PASS;
        }

        float absorptionAmount = player.getAbsorptionAmount();

        HealthBarRenderer.render(guiGraphics, player,
                player.getMaxHealth(), player.getHealth(),
                (int)absorptionAmount, deltaTracker);
        ClientAbstractions.INSTANCE.addGuiLeftHeight(minecraft.gui, ConfigConstants.HEALTH_BORDER_HEIGHT);

        return EventResult.INTERRUPT;
    }
}