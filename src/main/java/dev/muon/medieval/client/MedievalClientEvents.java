package dev.muon.medieval.client;

import dev.muon.medieval.Medieval;
import dev.muon.medieval.config.ConfigConstants;
import dev.muon.medieval.config.MedievalConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Medieval.MODID, value = Dist.CLIENT)
public class MedievalClientEvents {
    @SubscribeEvent
    public static void onBeforeRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        if (!MedievalConfig.get().enableCustomResourceBars) return;

        if (event.getOverlay() == VanillaGuiOverlay.PLAYER_HEALTH.type() ||
                event.getOverlay() == VanillaGuiOverlay.FOOD_LEVEL.type() /*||
                event.getOverlay() == VanillaGuiOverlay.ARMOR_LEVEL.type()*/) {
            event.setCanceled(true);
        }
    }
}