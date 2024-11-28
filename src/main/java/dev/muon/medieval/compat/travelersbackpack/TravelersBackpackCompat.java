package dev.muon.medieval.compat.travelersbackpack;

import com.tiviacz.travelersbackpack.fluids.EffectFluidRegistry;
import net.minecraftforge.fml.ModList;

public class TravelersBackpackCompat {
    public static void init() {
        if (ModList.get().isLoaded("travelersbackpack") && ModList.get().isLoaded("survive")) {
            EffectFluidRegistry.registerFluidEffect(new PurifiedWaterEffect());
        }
    }
}