package dev.muon.medieval.mixin.compat.apotheosis;

import dev.shadowsoffire.apothic_attributes.client.AttributesGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = AttributesGui.class, remap = false)
public class AttributesGuiMixin {
    @ModifyArg(
            method = "renderEntry",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;FFIZ)I"),
            index = 4,
            remap = false
    )
    private int modifyEntryTexts(int originalColor) {
        if (originalColor == 0x404040) {
            return 0xFFAA00;
        }
        return originalColor;
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"),
            index = 4,
            remap = true
    )
    private int modifyTopBottomTexts(int originalColor) {
        if (originalColor == 0x404040) {
            return 0xFFAA00;
        }
        return originalColor;
    }
}