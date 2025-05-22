package dev.muon.medieval.mixin.compat.starterkit;

import com.natamus.starterkit_common_fabric.inventory.StarterKitInventoryScreen;
import dev.muon.medieval.Medieval;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayerManager;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = StarterKitInventoryScreen.class, remap = true)
public class StarterKitMixin {
    @ModifyVariable(method = "renderLabels", at = @At(value = "STORE", ordinal = 1), name = "playerName")
    private String appendOriginToPlayerName(String playerName) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            OriginComponent component = ModComponents.ORIGIN.get(player);
            OriginLayer layer = OriginLayerManager.getNullable(ResourceLocation.fromNamespaceAndPath("origins", "origin"));
            if (layer != null) {
                Origin origin = component.getOrigin(layer);
                if (origin != null && origin != Origin.EMPTY) {
                    String originName = origin.getName().getString();
                    if (!originName.isEmpty()) {
                        return playerName + " the " + originName;
                           // maybe we could even have one more if block here for   m a x i m u m     d e p t h
                              // just imagine it
                              // seriously though, where is "getPrimaryOrigin(player)"
                           //
                        //
                    }
                }
            }
        }
        return playerName;
    }

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
