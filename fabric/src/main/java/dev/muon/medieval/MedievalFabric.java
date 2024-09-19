package dev.muon.medieval;

import net.fabricmc.api.ModInitializer;

public class MedievalFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Medieval.LOG.info("Hello Fabric world!");
        Medieval.init();
    }
}
