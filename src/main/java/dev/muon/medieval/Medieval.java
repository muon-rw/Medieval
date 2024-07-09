package dev.muon.medieval;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


public class Medieval implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("zephyr");


	@Override
	public void onInitialize() {
		LOGGER.info("Loading Medieval");
	}
	public static ResourceLocation loc(String id) {
		return new ResourceLocation("zephyr", id);
	}
}