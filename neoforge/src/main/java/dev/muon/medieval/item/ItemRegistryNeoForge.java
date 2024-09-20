package dev.muon.medieval.item;

import dev.muon.medieval.Medieval;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegistryNeoForge {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Medieval.MOD_ID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Medieval.MOD_ID);

    public static void init(IEventBus eventBus) {
        ITEMS.register(eventBus);
        DATA_COMPONENTS.register(eventBus);
        registerItems();
        registerDataComponents();
    }

    private static void registerItems() {
        ITEMS.register("challenge_orb", () -> {
            ItemRegistry.CHALLENGE_ORB = new ChallengeOrbItemNeoForge(
                    new Item.Properties()
                            .stacksTo(1)
                            .component(ChallengeOrbItemNeoForge.CHALLENGE_ORB_DATA.value(), new ChallengeOrbItem.ChallengeOrbData("", 0))
            );
            return ItemRegistry.CHALLENGE_ORB;
        });
        ITEMS.register("town_portal_scroll", () -> ItemRegistry.TOWN_PORTAL_SCROLL);
    }

    private static void registerDataComponents() {
        ChallengeOrbItemNeoForge.registerDataComponent(DATA_COMPONENTS);
    }
}