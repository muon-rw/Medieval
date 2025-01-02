package dev.muon.medieval.mixin.compat.dehydration;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.dehydration.config.DehydrationConfig;
import net.dehydration.init.ConfigInit;
import net.dehydration.misc.ThirstTooltipData;
import net.dehydration.thirst.ThirstHudRender;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(ThirstHudRender.class)
public class ThirstHudRenderMixin {
    @Redirect(
            method = "renderThirstHud",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/dehydration/config/DehydrationConfig;thirst_preview:Z"
            ),
            remap = false
    )
    private static boolean redirectThirstPreview(DehydrationConfig instance) {
        return false;
    }

}