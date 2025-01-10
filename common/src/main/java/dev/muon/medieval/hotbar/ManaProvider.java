package dev.muon.medieval.hotbar;

public interface ManaProvider {
    double getCurrentMana();
    float getMaxMana();
    float getReservedMana(); // For use by Ars Nouveau, not sure if other mana systems have similar concepts
    long getGameTime();  // For animation timing
}