package dev.muon.medieval.effect;

import com.stereowalker.survive.needs.IRealisticEntity;
import com.stereowalker.survive.needs.WaterData;
import com.stereowalker.survive.world.level.material.SFluids;
import com.tiviacz.travelersbackpack.api.fluids.EffectFluid;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

public class PurifiedWaterEffect extends EffectFluid {
    public PurifiedWaterEffect() {
        super("purified_water", SFluids.PURIFIED_WATER, 1000);
    }

    @Override
    public void affectDrinker(FluidStack fluidStack, Level level, Entity entity) {
        if (entity instanceof Player player) {
            WaterData waterData = ((IRealisticEntity) player).getWaterData();
            waterData.drink(6, 3.0F, 0, false);
            waterData.save(player);
        }
    }

    @Override
    public boolean canExecuteEffect(FluidStack stack, Level level, Entity entity) {
        if (entity instanceof Player player && stack.getAmount() >= this.amountRequired) {
             return ((IRealisticEntity)player).getWaterData().getWaterLevel() < 20;
        }
        return false;
    }
}
