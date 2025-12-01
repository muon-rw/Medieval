package dev.muon.medieval.mixin.compat.starterkit;

import com.natamus.starterkit_common_neoforge.inventory.StarterKitInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = StarterKitInventoryScreen.class, remap = true)
public class StarterKitMixin {

    @ModifyArg(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"), index = 4)
    private int modifyColor1(int color) {
        if (color == 4210752) {
            return 0xFFFFFF;
        }
        return color;
    }

    @ModifyArg(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"), index = 4)
    private int modifyColor2(int color) {
        if (color == 4210752) {
            return 0xFFFFFF;
        }
        return color;
    }

    @ModifyArg(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)I"), index = 4)
    private int modifyColor3(int color) {
        if (color == 4210752) {
            return 0xFFAA00;
        }
        return color;
    }
}
