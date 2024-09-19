package dev.muon.medieval.platform;

public class Services {
    public static MedievalPlatformHelper PLATFORM;

    public static void setup(MedievalPlatformHelper platformHelper) {
        PLATFORM = platformHelper;
    }
}