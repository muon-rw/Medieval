package dev.muon.medieval;

import dev.muon.medieval.hotbar.compat.OverflowingBarsCompat;
import dev.muon.medieval.item.ItemRegistry;
import dev.muon.medieval.item.ItemRegistryNeoForge;
import dev.muon.medieval.platform.MedievalPlatformHelperNeoForge;
import dev.muon.medieval.platform.Services;
import fuzs.puzzleslib.api.client.event.v1.gui.RenderGuiLayerEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Medieval.MOD_ID)
public class MedievalNeoForge {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Medieval.MOD_ID);

    public MedievalNeoForge(IEventBus eventBus) {
        Medieval.init();
        Medieval.setHelper(new MedievalPlatformHelperNeoForge());
        Services.setup(new MedievalPlatformHelperNeoForge());

        ItemRegistryNeoForge.init(eventBus);

        registerCreativeTabs();
        CREATIVE_MODE_TABS.register(eventBus);

        initializeCompat();
    }

    public void initializeCompat() {
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient() && isModLoaded("overflowingbars")) {
            RenderGuiLayerEvents.before(RenderGuiLayerEvents.PLAYER_HEALTH)
                    .register(OverflowingBarsCompat::onRenderPlayerHealth);
        }
    }
    public boolean isModLoaded(String modId) {
        if (ModList.get() == null) {
            return LoadingModList.get().getMods().stream().map(ModInfo::getModId).anyMatch(modId::equals);
        }
        return ModList.get().isLoaded(modId);
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