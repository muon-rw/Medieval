package dev.muon.medieval.item;

import dev.muon.medieval.Medieval;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ItemRegistryFabric {
    public static void init() {
        ItemRegistry.init();
        ItemRegistry.CHALLENGE_ORB = register("challenge_orb", new ChallengeOrbItemFabric(new Item.Properties().stacksTo(1)));
        register("town_portal_scroll", ItemRegistry.TOWN_PORTAL_SCROLL);

        ChallengeOrbItemFabric.registerDataComponent();
    }

    private static <T extends Item> T register(String name, T item) {
        return Registry.register(BuiltInRegistries.ITEM, Medieval.loc(name), item);
    }
}