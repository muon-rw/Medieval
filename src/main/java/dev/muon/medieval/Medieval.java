package dev.muon.medieval;

import dev.muon.medieval.quest.TaskTypes;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Medieval implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("medieval");

	@Override
	public void onInitialize() {
		LOGGER.info("Loading Medieval");
		TaskTypes.init();
	}

	public static ResourceLocation loc(String id) {
		return ResourceLocation.fromNamespaceAndPath("medieval", id);
	}
}