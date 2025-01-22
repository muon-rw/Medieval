package dev.muon.medieval.mixin.compat.ice_and_fire;

import com.github.alexthe666.iceandfire.entity.props.FrozenData;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FrozenData.class, remap = false)
public class FrozenDataMixin {
    @Shadow
    private void clearFrozen(LivingEntity entity) {}

    @Inject(method = "tickFrozen", at = @At("HEAD"), cancellable = true)
    private void checkFrostwardRing(LivingEntity entity, CallbackInfo ci) {
        if (entity instanceof ServerPlayer player && ItemRegistry.FROSTWARD_RING.get().isEquippedBy(player)) {
            clearFrozen(entity);
            ci.cancel();
        }
    }
}