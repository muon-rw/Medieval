package dev.muon.medieval;


import dev.muon.medieval.platform.ExamplePlatformHelperNeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Medieval.MOD_ID)
public class MedievalNeoForge {

    public MedievalNeoForge(IEventBus eventBus) {
        Medieval.init();
        Medieval.setHelper(new ExamplePlatformHelperNeoForge());
    }
}