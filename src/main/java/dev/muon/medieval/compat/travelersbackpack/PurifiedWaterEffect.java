package dev.muon.medieval.compat.travelersbackpack;

import com.stereowalker.survive.needs.IRealisticEntity;
import com.stereowalker.survive.needs.WaterData;
import com.stereowalker.survive.world.level.material.SFluids;
import com.tiviacz.travelersbackpack.api.fluids.EffectFluid;
import com.tiviacz.travelersbackpack.util.Reference;
import dev.muon.medieval.Medieval;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

public class PurifiedWaterEffect extends EffectFluid {
    public PurifiedWaterEffect() {
        super("survive:purified_water", SFluids.PURIFIED_WATER, Reference.BUCKET);
    }

    @Override
    public void affectDrinker(FluidStack fluidStack, Level level, Entity entity) {
        if (entity instanceof Player player) {
            WaterData waterData = ((IRealisticEntity) player).waterData();
            waterData.drink(6, 3.0F, 0, false);
            waterData.save(player);
        }
    }

    @Override
    public boolean canExecuteEffect(FluidStack stack, Level level, Entity entity) {
        Medieval.LOGGER.info("Current Value: {}, Require Value: {}", stack.getAmount(), Reference.BUCKET);
        return true;
    }
}
