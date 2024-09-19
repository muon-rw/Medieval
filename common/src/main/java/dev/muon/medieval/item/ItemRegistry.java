package dev.muon.medieval.item;

import net.minecraft.world.item.Item;

public class ItemRegistry {
    public static ChallengeOrbItem CHALLENGE_ORB;
    public static final TownPortalScrollItem TOWN_PORTAL_SCROLL = new TownPortalScrollItem(new Item.Properties().stacksTo(16));

    public static void init() {
        // This method will be called by loader-specific registries
    }
}