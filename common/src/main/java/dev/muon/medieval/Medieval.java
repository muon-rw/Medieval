package dev.muon.medieval;

import dev.muon.medieval.platform.MedievalPlatformHelper;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Medieval {
    public static final String MOD_ID = "medieval";
    public static final Logger LOG = LoggerFactory.getLogger("Medieval");

    private static MedievalPlatformHelper helper;

    public static void init() {

    }

    public static ResourceLocation loc(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    public static MedievalPlatformHelper getHelper() {
        return helper;
    }

    public static void setHelper(MedievalPlatformHelper helper) {
        Medieval.helper = helper;
    }
}