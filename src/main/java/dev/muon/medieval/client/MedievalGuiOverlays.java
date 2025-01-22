package dev.muon.medieval.client;

import dev.muon.medieval.config.ConfigConstants;
import dev.muon.medieval.config.MedievalConfig;
import dev.muon.medieval.hotbar.HealthBarRenderer;
import dev.muon.medieval.hotbar.StaminaBarRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class MedievalGuiOverlays {
    public static final IGuiOverlay MEDIEVAL_BARS = (ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) -> {
        Minecraft minecraft = gui.getMinecraft();
        if (!MedievalConfig.get().enableCustomResourceBars) return;
        if (minecraft.options.hideGui) return;
        if (!gui.shouldDrawSurvivalElements()) return;

        var player = minecraft.player;
        if (player == null) return;

        HealthBarRenderer.render(graphics, player,
                player.getMaxHealth(), player.getHealth(),
                (int)player.getAbsorptionAmount(), partialTick);
        StaminaBarRenderer.render(graphics, player, partialTick);
        //ArmorBarRenderer.render(graphics, player);

        gui.leftHeight += ConfigConstants.HEALTH_BORDER_HEIGHT + 1;
        //gui.leftHeight += 9 + 1;
        gui.rightHeight += ConfigConstants.STAMINA_BORDER_HEIGHT + 1;
    };
}