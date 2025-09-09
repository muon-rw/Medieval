package dev.muon.medieval.mixin.compat.mobhealthbar;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.minecraftserverzone.mobhealthbar.ForgeRegistryEvents;
import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.mobs.MagicSummon;
import net.mehvahdjukaar.dummmmmmy.common.TargetDummyEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.RenderNameTagEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ForgeRegistryEvents.class, remap = false)
public class ForgeRegistryEventsMixin {
    @ModifyVariable(method = "renderHpBar", at = @At(value = "STORE"), ordinal = 8)
    private static boolean modifyShouldShow(boolean original, @Local(argsOnly = true) RenderNameTagEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof TargetDummyEntity ||
                (entity instanceof AntiMagicSusceptible && !(entity instanceof MagicSummon))) {
            return false;
        }
        return original;
    }

    @WrapOperation(method = "renderHpBar",
            at = @At(value = "INVOKE",
                    target = "Lcom/minecraftserverzone/mobhealthbar/GuiHelper;drawString(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V",
            ordinal = 0))
    private static void alwaysHideName(PoseStack stack, Font font, String text, int x, int y, int color, boolean shadow, Operation<Void> original) {;
        // Do nothing and don't return original
    }
}