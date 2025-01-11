package dev.muon.medieval.platform;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

public class MedievalPlatformHelperNeoForge implements MedievalPlatformHelper {

    @Override
    public Platform getPlatform() {
        return Platform.NEOFORGE;
    }

    @Override
    public boolean isModLoaded(String modId) {
        if (ModList.get() == null) {
        return LoadingModList.get().getMods().stream().map(ModInfo::getModId).anyMatch(modId::equals);
    }
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public FTBHelper getFTBHelper() {
        return new FTBHelperNeoForge();
    }
}