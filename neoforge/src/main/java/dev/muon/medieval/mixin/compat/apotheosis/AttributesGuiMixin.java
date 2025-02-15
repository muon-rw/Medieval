package dev.muon.medieval.mixin.compat.apotheosis;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.shadowsoffire.apothic_attributes.client.AttributesGui;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;
import java.util.stream.Stream;

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

    @Shadow
    protected static boolean hideUnchanged;

    @SuppressWarnings("unchecked")
    @WrapOperation(
            method = "refreshData",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"
            ),
            remap = false
    )
    private <T> Stream<T> modifyHideUnchangedFilter(Stream<T> instance, Predicate<? super T> predicate, Operation<Stream<T>> original) {
        Stream<AttributeInstance> filtered = (Stream<AttributeInstance>)original.call(instance, predicate);

        if (hideUnchanged) {
            filtered = filtered.filter(ai -> !ai.getModifiers().isEmpty());
        }

        return (Stream<T>) filtered;
    }
}