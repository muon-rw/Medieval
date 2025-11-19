package dev.muon.medieval.client.combat;

/**
 * Holds client-side combat timing state for Reactive Music integration.
 */
public class CombatMusicState {

    /** Game tick the player last attacked a LivingEntity. */
    public static long lastPlayerAttackTime = -1;

    /** Game tick the player was last damaged by a Monster. */
    public static long lastPlayerDamagedByHostileTime = -1;

    private CombatMusicState() {}
} 