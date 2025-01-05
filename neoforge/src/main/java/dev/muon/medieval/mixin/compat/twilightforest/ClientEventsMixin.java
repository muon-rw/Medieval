package dev.muon.medieval.mixin.compat.twilightforest;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.event.ClientEvents;
import twilightforest.compat.curios.CuriosCompat;
import twilightforest.item.SkullCandleItem;
import twilightforest.item.TrophyItem;

@Mixin(ClientEvents.class)
public class ClientEventsMixin {

    @Unique
    private static boolean medieval$areCuriosEquipped(LivingEntity entity) {
        if (!ModList.get().isLoaded("curios")) {
            return false;
        } else {
            return CuriosCompat.isCurioEquippedAndVisible(entity, (stack) -> {
                return stack.getItem() instanceof TrophyItem;
            }) || CuriosCompat.isCurioEquippedAndVisible(entity, (stack) -> {
                return stack.getItem() instanceof SkullCandleItem;
            });
        }
    }

    @Inject(method = "unrenderHeadWithTrophies", at = @At("HEAD"), cancellable = true)
    private static void onUnrenderHeadWithTrophies(RenderLivingEvent.Pre<?, ?> event, CallbackInfo ci) {
        boolean shouldHideHead = medieval$areCuriosEquipped(event.getEntity());

        EntityModel<?> model = event.getRenderer().getModel();
        if (model instanceof HeadedModel headedModel) {
            boolean wasHeadVisible = headedModel.getHead().visible;
            headedModel.getHead().visible = wasHeadVisible && !shouldHideHead;

            if (model instanceof HumanoidModel<?> humanoidModel) {
                boolean wasHatVisible = humanoidModel.hat.visible;
                humanoidModel.hat.visible = wasHatVisible && !shouldHideHead;
            }
        }
        ci.cancel();
    }
}