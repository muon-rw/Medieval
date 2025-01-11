package dev.muon.medieval;

import dev.muon.medieval.compat.OverflowingBarsCompat;
import dev.muon.medieval.item.ItemRegistry;
import dev.muon.medieval.item.ItemRegistryFabric;
import dev.muon.medieval.quest.TaskTypes;
import fuzs.puzzleslib.api.client.event.v1.gui.RenderGuiLayerEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class MedievalFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Medieval.LOG.info("Hello Fabric world!");
        Medieval.init();

        TaskTypes.init();
        ItemRegistryFabric.init();
        registerCreativeTabs();
        initializeCompat();
    }

    public void initializeCompat() {
        if (FabricLoader.getInstance().isModLoaded("overflowingbars")) {
            RenderGuiLayerEvents.before(RenderGuiLayerEvents.PLAYER_HEALTH)
                    .register(OverflowingBarsCompat::onRenderPlayerHealth);
        }
    }

    private void registerCreativeTabs() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Medieval.loc("medieval_tab"),
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(ItemRegistry.CHALLENGE_ORB))
                        .title(Component.translatable("itemGroup.medieval"))
                        .displayItems((parameters, output) -> {
                            output.accept(ItemRegistry.CHALLENGE_ORB);
                            output.accept(ItemRegistry.TOWN_PORTAL_SCROLL);
                        })
                        .build());
    }
}