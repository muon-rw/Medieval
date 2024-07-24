package dev.muon.medieval.item;

import dev.muon.medieval.Medieval;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Medieval.MODID);
    public static final RegistryObject<Item> CHALLENGE_ORB = ITEMS.register("challenge_orb",
            () -> new ChallengeOrbItem(new Item.Properties().stacksTo(16)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}