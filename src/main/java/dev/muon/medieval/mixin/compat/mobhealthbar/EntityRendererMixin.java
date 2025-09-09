package dev.muon.medieval.mixin.compat.mobhealthbar;

import com.llamalad7.mixinextras.sugar.Local;
import com.minecraftserverzone.mobhealthbar.configs.HpBarModConfig;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    // These constants are derived from the previous LevelDisplayRenderer behavior
    // to replicate its name tag positioning.
    @Unique
    private static final float PREVIOUS_RENDERER_BASE_Y_TRANSLATE_ADDITION = 1.058F;
    @Unique
    private static final float PREVIOUS_RENDERER_TEXT_SCALE = -0.02F;

    @ModifyArg(
            method = "renderNameTag(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"),
            index = 1
    )
    private float adjustNameTagY(float originalY, @Local(argsOnly = true) T pEntity) {
        if (pEntity instanceof LivingEntity) {

            // 1. Calculate the initial Y reference point based on the entity's height,
            //    using the constant from the old renderer.
            float yReferencePoint = pEntity.getBbHeight() + PREVIOUS_RENDERER_BASE_Y_TRANSLATE_ADDITION;

            // 2. Get the configured Y offset from HpBarModConfig. This value is typically
            //    an integer representing a pixel offset.
            int posYAddFromConfig = HpBarModConfig.HP_BAR_TYPE[2].get();

            // 3. Apply the old text scaling factor to this configured offset to determine
            //    its effect in world units, consistent with the old renderer.
            float scaledAdjustment = posYAddFromConfig * PREVIOUS_RENDERER_TEXT_SCALE;

            // 4. The final target Y for the translate call is the reference point plus the scaled adjustment.
            float finalTargetY = yReferencePoint + scaledAdjustment;

            return finalTargetY;
        }

        // For players or other entity types, or if conditions are not met,
        // use the original Y translation argument determined by vanilla.
        return originalY;
    }
}