package dev.muon.medieval.platform;

import dev.muon.medieval.config.MedievalConfigHelper;

public interface MedievalPlatformHelper {

    /**
     * Gets the current platform
     *
     * @return An enum value representing the current platform.
     */
    Platform getPlatform();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the FTB helper for the current platform.
     *
     * @return An instance of FTBHelper for the current platform.
     */
    FTBHelper getFTBHelper();


}