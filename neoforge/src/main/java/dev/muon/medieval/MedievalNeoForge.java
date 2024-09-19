package dev.muon.medieval;

import dev.muon.medieval.item.ItemRegistry;
import dev.muon.medieval.item.ItemRegistryNeoForge;
import dev.muon.medieval.platform.MedievalPlatformHelperNeoForge;
import dev.muon.medieval.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Medieval.MOD_ID)
public class MedievalNeoForge {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Medieval.MOD_ID);

    public MedievalNeoForge(IEventBus eventBus) {
        Medieval.init();
        Medieval.setHelper(new MedievalPlatformHelperNeoForge());
        Services.setup(new MedievalPlatformHelperNeoForge());

        ItemRegistryNeoForge.init();
        ItemRegistryNeoForge.ITEMS.register(eventBus);
        ItemRegistryNeoForge.registerDataComponents();
        ItemRegistryNeoForge.DATA_COMPONENTS.register(eventBus);

        registerCreativeTabs();
        CREATIVE_MODE_TABS.register(eventBus);
    }

    private void registerCreativeTabs() {
        CREATIVE_MODE_TABS.register("medieval_tab", () -> CreativeModeTab.builder()
                .icon(() -> new ItemStack(ItemRegistry.CHALLENGE_ORB))
                .title(Component.translatable("itemGroup.medieval"))
                .displayItems((parameters, output) -> {
                    output.accept(ItemRegistry.CHALLENGE_ORB);
                    output.accept(ItemRegistry.TOWN_PORTAL_SCROLL);
                })
                .build());
    }
}