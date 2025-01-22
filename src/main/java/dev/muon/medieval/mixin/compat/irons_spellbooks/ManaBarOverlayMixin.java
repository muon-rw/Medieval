package dev.muon.medieval.mixin.compat.irons_spellbooks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.muon.medieval.compat.irons_spellbooks.IronsSpellbooksManaProvider;
import dev.muon.medieval.hotbar.ManaBarRenderer;
import io.redspace.ironsspellbooks.gui.overlays.ManaBarOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ManaBarOverlay.class, remap = false)
public class ManaBarOverlayMixin {
    @Unique
    private static final IronsSpellbooksManaProvider PROVIDER = new IronsSpellbooksManaProvider();

    @ModifyReturnValue(method = "shouldShowManaBar", at = @At("RETURN"))
    private static boolean fadeOut(boolean original) {
        boolean setToHide = !Minecraft.getInstance().options.hideGui && original;
        ManaBarRenderer.setBarVisibility(setToHide);
        return ManaBarRenderer.isVisible();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void renderCustomManaBar(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight, CallbackInfo ci) {
        if (gui.getMinecraft().player == null) return;
        if (!ManaBarOverlay.shouldShowManaBar(gui.getMinecraft().player)) return;
        if (gui.getMinecraft().options.hideGui) return;

        ManaBarRenderer.render(guiGraphics, partialTick, PROVIDER);
        ci.cancel();
    }
}
