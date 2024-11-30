package dev.muon.medieval.leveling;

import dev.muon.medieval.Medieval;
import dev.muon.medieval.leveling.client.LevelDisplayRenderer;
import dev.muon.medieval.leveling.event.AptitudeChangedEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.UUID;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Medieval.MODID)
public class LevelSyncHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Medieval.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            INSTANCE.registerMessage(0, SyncPlayerLevelPacket.class, SyncPlayerLevelPacket::encode, SyncPlayerLevelPacket::decode, SyncPlayerLevelPacket::handle);
        });
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide) {
            int level = LevelingUtils.getPlayerLevel(event.getEntity());
            INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncPlayerLevelPacket(event.getEntity().getUUID(), level));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.getEntity().level().isClientSide) {
            int level = LevelingUtils.getPlayerLevel(event.getEntity());
            INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncPlayerLevelPacket(event.getEntity().getUUID(), level));
        }
    }

    @SubscribeEvent
    public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!event.getEntity().level().isClientSide) {
            int level = LevelingUtils.getPlayerLevel(event.getEntity());
            INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncPlayerLevelPacket(event.getEntity().getUUID(), level));
        }
    }

    @SubscribeEvent
    public static void onAptitudeChanged(AptitudeChangedEvent event) {
        if (!event.getPlayer().level().isClientSide) {
            int newLevel = LevelingUtils.getPlayerLevel(event.getPlayer());
            INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncPlayerLevelPacket(event.getPlayer().getUUID(), newLevel));
        }
    }

    public static class SyncPlayerLevelPacket {
        public final UUID playerId;
        public final int level;

        public SyncPlayerLevelPacket(UUID playerId, int level) {
            this.playerId = playerId;
            this.level = level;
        }

        public static void encode(SyncPlayerLevelPacket packet, FriendlyByteBuf buffer) {
            buffer.writeUUID(packet.playerId);
            buffer.writeInt(packet.level);
        }

        public static SyncPlayerLevelPacket decode(FriendlyByteBuf buffer) {
            return new SyncPlayerLevelPacket(buffer.readUUID(), buffer.readInt());
        }

        public static void handle(SyncPlayerLevelPacket packet, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                LevelDisplayRenderer.updatePlayerLevel(packet.playerId, packet.level);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}