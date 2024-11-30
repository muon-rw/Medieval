package dev.muon.medieval.leveling;

import dev.muon.medieval.Medieval;
import dev.muon.medieval.leveling.event.AptitudeChangedEvent;
import dev.muon.medieval.network.SyncPlayerLevelPacket;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Medieval.MODID)
public class LevelSyncHandler {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide) {
            int level = LevelingUtils.getPlayerLevel(event.getEntity());
            Medieval.NETWORK.send(PacketDistributor.ALL.noArg(), new SyncPlayerLevelPacket(event.getEntity().getUUID(), level));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.getEntity().level().isClientSide) {
            int level = LevelingUtils.getPlayerLevel(event.getEntity());
            Medieval.NETWORK.send(PacketDistributor.ALL.noArg(), new SyncPlayerLevelPacket(event.getEntity().getUUID(), level));
        }
    }

    @SubscribeEvent
    public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!event.getEntity().level().isClientSide) {
            int level = LevelingUtils.getPlayerLevel(event.getEntity());
            Medieval.NETWORK.send(PacketDistributor.ALL.noArg(), new SyncPlayerLevelPacket(event.getEntity().getUUID(), level));
        }
    }

    @SubscribeEvent
    public static void onAptitudeChanged(AptitudeChangedEvent event) {
        if (!event.getPlayer().level().isClientSide) {
            int newLevel = LevelingUtils.getPlayerLevel(event.getPlayer());
            Medieval.NETWORK.send(PacketDistributor.ALL.noArg(), new SyncPlayerLevelPacket(event.getPlayer().getUUID(), newLevel));
        }
    }

}