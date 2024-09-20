package dev.muon.medieval.platform;

import net.fabricmc.loader.api.FabricLoader;

public class MedievalPlatformHelperFabric implements MedievalPlatformHelper {

    @Override
    public Platform getPlatform() {
        return Platform.FABRIC;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public FTBHelper getFTBHelper() {
        return new FTBHelperFabric();
    }

}
