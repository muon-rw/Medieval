package dev.muon.medieval;

public class ManaTextHelper {
    //TODO: Fix this on dedicated servers
    private static long lastManaErrorTime = 0;

    public static void onManaError() {
        lastManaErrorTime = System.currentTimeMillis();
    }

    public static long getLastManaErrorTime() {
        return lastManaErrorTime;
    }
}