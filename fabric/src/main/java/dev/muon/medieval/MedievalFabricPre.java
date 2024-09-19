package dev.muon.medieval;

import dev.muon.medieval.platform.ExamplePlatformHelperFabric;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class MedievalFabricPre implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        Medieval.setHelper(new ExamplePlatformHelperFabric());
    }
}
