package dev.muon.medieval;

import dev.muon.medieval.leveling.OriginalLevelCapability;
import dev.muon.medieval.leveling.client.LevelDisplayRenderer;
import dev.muon.medieval.leveling.DynamicMobLeveling;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import dev.muon.medieval.item.ItemRegistry;


@Mod(Medieval.MODID)
public class Medieval
{
    public static final String MODID = "medieval";
    private static final Logger LOGGER = LogManager.getLogger();
    public Medieval()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        Medieval.LOGGER.info("Medieval MC Forge Tweaks loading");

        ItemRegistry.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);

        //MinecraftForge.EVENT_BUS.register(DynamicMobLeveling.class);
        MinecraftForge.EVENT_BUS.register(OriginalLevelCapability.class);
        //MinecraftForge.EVENT_BUS.register(LevelDisplayRenderer.class);
    }
    private void commonSetup(final FMLCommonSetupEvent event)
    {}

    public static ResourceLocation loc(String id) {
        return new ResourceLocation(Medieval.MODID, id);
    }
}