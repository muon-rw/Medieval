package dev.muon.medieval.item;

import dev.muon.medieval.Medieval;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Medieval.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Medieval.MODID);

    public static final RegistryObject<Item> CHALLENGE_ORB = ITEMS.register("challenge_orb",
            () -> new ChallengeOrbItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> TOWN_PORTAL_SCROLL = ITEMS.register("town_portal_scroll",
            () -> new TownPortalScrollItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<CreativeModeTab> MEDIEVAL_TAB = CREATIVE_MODE_TABS.register("medieval_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(CHALLENGE_ORB.get()))
                    .title(Component.translatable("itemGroup.medieval"))
                    .displayItems((parameters, output) -> {
                        output.accept(CHALLENGE_ORB.get());
                        output.accept(TOWN_PORTAL_SCROLL.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus);
    }
}