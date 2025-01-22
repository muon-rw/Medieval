package dev.muon.medieval.hotbar;

public interface ManaProvider {
    double getCurrentMana();
    float getMaxMana();
    float getReservedMana();
    long getGameTime();
}