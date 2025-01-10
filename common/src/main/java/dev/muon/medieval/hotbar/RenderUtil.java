package dev.muon.medieval.hotbar;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class RenderUtil {
    public static final long TEXT_DISPLAY_DURATION = 2000L;
    public static final long TEXT_FADEOUT_DURATION = 500L;
    public static final int BASE_TEXT_ALPHA = 200;
    // Only used for the mana bar
    public static final long BAR_FADEOUT_DURATION = 1500L;

    public static void renderText(float current, float max, GuiGraphics graphics, int baseX, int baseY, int color) {
        Minecraft minecraft = Minecraft.getInstance();
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        float scalingFactor = ConfigConstants.TEXT_SCALING_FACTOR;

        int xPos = (int) (baseX / scalingFactor);
        int yPos = (int) (baseY / scalingFactor);
        poseStack.scale(scalingFactor, scalingFactor, 1.0f);

        String currentText = String.valueOf((int)current);
        String maxText = String.valueOf((int)max);
        String slashText = "/";

        int slashWidth = minecraft.font.width(slashText);
        int currentWidth = minecraft.font.width(currentText);
        int slashX = xPos - (slashWidth / 2);

        graphics.drawString(minecraft.font, currentText, slashX - currentWidth, yPos, color, true);
        graphics.drawString(minecraft.font, slashText, slashX, yPos, color, true);
        graphics.drawString(minecraft.font, maxText, slashX + slashWidth, yPos, color, true);

        poseStack.popPose();
    }

    public static int calculateTextAlpha(long timeSinceEvent, long displayDuration, long fadeoutDuration, int baseAlpha) {
        int alpha;
        if (timeSinceEvent < displayDuration) {
            alpha = timeSinceEvent > displayDuration - fadeoutDuration
                    ? (int)(baseAlpha * (displayDuration - timeSinceEvent) / fadeoutDuration)
                    : baseAlpha;
        } else {
            alpha = 0;
        }
        // Alpha values below 10 cause rendering inconsistencies, not sure why
        return Math.max(10, Math.min(alpha, baseAlpha));
    }

}