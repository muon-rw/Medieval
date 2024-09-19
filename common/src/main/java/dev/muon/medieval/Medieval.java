package dev.muon.medieval;

import dev.muon.medieval.platform.ExamplePlatformHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Medieval {
    public static final String MOD_ID = "medieval";
    public static final Logger LOG = LoggerFactory.getLogger("Medieval");

    private static ExamplePlatformHelper helper;

    public static void init() {

    }

    public static ExamplePlatformHelper getHelper() {
        return helper;
    }

    public static void setHelper(ExamplePlatformHelper helper) {
        Medieval.helper = helper;
    }
}