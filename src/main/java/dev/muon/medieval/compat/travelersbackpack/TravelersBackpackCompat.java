package dev.muon.medieval.compat.travelersbackpack;

import com.tiviacz.travelersbackpack.fluids.EffectFluidRegistry;
import dev.muon.medieval.Medieval;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

public class TravelersBackpackCompat {
    public static void init() {
        if (isModLoaded("travelersbackpack") && isModLoaded("survive")) {
            EffectFluidRegistry.registerFluidEffect(new PurifiedWaterEffect());
        } else {
            Medieval.LOGGER.warn("Tried to register Survive compat but mod loaded check failed");
        }
    }
    private static boolean isModLoaded(String modId) {
        if (ModList.get() == null) {
            return LoadingModList.get().getMods().stream().map(ModInfo::getModId).anyMatch(modId::equals);
        }
        return ModList.get().isLoaded(modId);
    }
}