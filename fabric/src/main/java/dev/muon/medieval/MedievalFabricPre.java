package dev.muon.medieval;

import dev.muon.medieval.platform.MedievalPlatformHelperFabric;
import dev.muon.medieval.platform.Services;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class MedievalFabricPre implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        Medieval.setHelper(new MedievalPlatformHelperFabric());
        Services.setup(new MedievalPlatformHelperFabric());
    }
}