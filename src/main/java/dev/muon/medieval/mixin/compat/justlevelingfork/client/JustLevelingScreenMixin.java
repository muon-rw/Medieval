package dev.muon.medieval.mixin.compat.justlevelingfork.client;
import com.llamalad7.mixinextras.sugar.Local;
import com.seniors.justlevelingfork.client.core.Utils;
import com.seniors.justlevelingfork.client.screen.JustLevelingScreen;
import dev.muon.medieval.leveling.LevelingUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = JustLevelingScreen.class, remap = false)
public class JustLevelingScreenMixin {
    @Redirect(
            method = "drawAptitudes",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/seniors/justlevelingfork/client/core/Utils;drawCenter(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/network/chat/Component;II)V",
                    ordinal = 1
            )
    )
    private void renderFakeLevel(GuiGraphics matrixStack, Component string, int x, int y, @Local(argsOnly = true) GuiGraphics graphics) {
        JustLevelingScreen screen = (JustLevelingScreen) (Object) this;
        Player player = screen.getMinecraft().player;
        if (player != null) {
            int level = LevelingUtils.getPlayerLevel(player);
            double progress = LevelingUtils.getPlayerLevelProgress(player);
            Component newComponent = Component.translatable("screen.aptitude.level.custom", level, String.format("%.1f", progress));
            Utils.drawCenter(graphics, newComponent, x, y);
        }
    }
}