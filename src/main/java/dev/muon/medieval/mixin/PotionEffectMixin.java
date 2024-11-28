package dev.muon.medieval.mixin;

import com.stereowalker.survive.needs.IRealisticEntity;
import com.stereowalker.survive.needs.WaterData;
import com.tiviacz.travelersbackpack.fluids.effects.PotionEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PotionEffect.class, remap = false)
public class PotionEffectMixin {
    @Inject(method = "affectDrinker", at = @At("TAIL"))
    private void affectDrinker(FluidStack fluidStack, Level level, Entity entity, CallbackInfo ci) {
        if (entity instanceof Player player) {
            WaterData waterData = ((IRealisticEntity)player).getWaterData();
            waterData.drink(4, 1.0F, 0, true);
            waterData.save(player);
        }
    }
}
