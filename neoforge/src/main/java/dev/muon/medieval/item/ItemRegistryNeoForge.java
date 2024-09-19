package dev.muon.medieval.item;

import dev.muon.medieval.Medieval;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegistryNeoForge {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Medieval.MOD_ID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Medieval.MOD_ID);

    public static void init() {
        ItemRegistry.init();
        ITEMS.register("challenge_orb", () -> {
            ItemRegistry.CHALLENGE_ORB = new ChallengeOrbItemNeoForge(new Item.Properties().stacksTo(1));
            return ItemRegistry.CHALLENGE_ORB;
        });
        ITEMS.register("town_portal_scroll", () -> ItemRegistry.TOWN_PORTAL_SCROLL);
    }

    public static void registerDataComponents() {
        ChallengeOrbItemNeoForge.registerDataComponent(DATA_COMPONENTS);
    }
}