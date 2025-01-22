package dev.muon.medieval.compat.overflowingbars;

public class OverflowingBarsCompat {
    // Unused, not needed with Forge events
    /*
    public static EventResult onRenderPlayerHealth(Minecraft minecraft, GuiGraphics guiGraphics, float v, int i, int i1) {
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui) {
            return EventResult.PASS;
        }

        float absorptionAmount = player.getAbsorptionAmount();

        HealthBarRenderer.render(guiGraphics, player,
                player.getMaxHealth(), player.getHealth(),
                (int)absorptionAmount, v);
        //ClientAbstractions.INSTANCE.addGuiLeftHeight(minecraft.gui, ConfigConstants.HEALTH_BORDER_HEIGHT);

        return EventResult.INTERRUPT;
    }


     */
}