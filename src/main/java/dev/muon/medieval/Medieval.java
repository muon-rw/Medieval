package dev.muon.medieval;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Medieval implements ModInitializer {
    public static final String MOD_ID = "medieval";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation loc(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
    }
}