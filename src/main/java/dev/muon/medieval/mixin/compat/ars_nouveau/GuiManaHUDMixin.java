package dev.muon.medieval.mixin.compat.ars_nouveau;

import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.client.gui.GuiManaHUD;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.muon.medieval.compat.ars_nouveau.ArsNouveauManaProvider;
import dev.muon.medieval.hotbar.ManaBarRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiManaHUD.class, remap = false)
public class GuiManaHUDMixin {
    @Shadow @Final
    private static Minecraft minecraft;

    @ModifyReturnValue(method = "shouldDisplayBar", at = @At("RETURN"))
    private static boolean fadeOut(boolean original) {
        boolean setToHide = !minecraft.options.hideGui && original;
        ManaBarRenderer.setBarVisibility(setToHide);
        return ManaBarRenderer.isVisible();
    }

    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
    private static void renderCustomManaBar(ForgeGui gui, GuiGraphics guiGraphics, float pt, int width, int height, CallbackInfo ci) {
        if (!GuiManaHUD.shouldDisplayBar()) return;
        if (minecraft.options.hideGui) return;

        Player player = minecraft.player;
        if (player == null) return;

        LazyOptional<IManaCap> manaCapOpt = CapabilityRegistry.getMana(player);
        if (manaCapOpt.resolve().isEmpty()) return;

        IManaCap manaCap = manaCapOpt.resolve().get();
        if (manaCap.getMaxMana() == 0) return;

        ManaBarRenderer.render(guiGraphics, pt, new ArsNouveauManaProvider(manaCap));
        ci.cancel();
    }
}