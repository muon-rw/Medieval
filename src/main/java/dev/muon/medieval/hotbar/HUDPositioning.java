package dev.muon.medieval.hotbar;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.gui.overlay.ForgeGui;

public class HUDPositioning {
    // Base anchor points (relative to screen dimensions)
    public static Position getHealthAnchor() {
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        return new Position(
                (screenWidth / 2) - 91, // Vanilla health bar X - this is the left edge, expecting usage of align-left
                screenHeight - 40       // Vanilla health bar Y
        );
    }

    public static Position getArmorAnchor() {
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        return new Position(
                (screenWidth / 2) - 91,
                screenHeight - 51
        );
    }

    public static Position getHungerAnchor() {
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        return new Position(
                (screenWidth / 2) + 91, // Vanilla hunger bar X - this is the right edge, expecting usage of align-right
                screenHeight - 40       // Vanilla hunger bar Y
        );
    }

    public static Position getAboveUtilitiesAnchor() {
        Minecraft minecraft = Minecraft.getInstance();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        ForgeGui gui = (ForgeGui) minecraft.gui;

        int maxOffset = Math.max(gui.leftHeight, gui.rightHeight);

        return new Position(
                screenWidth / 2,                    // Center X
                screenHeight - (maxOffset + 12)     // Dynamic Y position based on HUD state
                // +26 for some padding above the highest element
        );
    }

    // Config offsets (to be implemented)
    public static int getManaBarXOffset() {
        return 0; // TODO: Load from config
    }

    public static int getManaBarYOffset() {
        return 0; // TODO: Load from config
    }

    public static int getHealthBarXOffset() {
        return 0; // TODO: Load from config
    }

    public static int getHealthBarYOffset() {
        return 0; // TODO: Load from config
    }

    public static int getStaminaBarXOffset() {
        return 0; // TODO: Load from config
    }

    public static int getStaminaBarYOffset() {
        return 0; // TODO: Load from config
    }

    public static int getArmorBarXOffset() {
        return 0; // TODO: Load from config
    }

    public static int getArmorBarYOffset() {
        return 0; // TODO: Load from config
    }
}