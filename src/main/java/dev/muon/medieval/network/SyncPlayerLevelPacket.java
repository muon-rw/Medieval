package dev.muon.medieval.network;

import dev.muon.medieval.leveling.LevelSyncHandler;
import dev.muon.medieval.leveling.client.LevelDisplayRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncPlayerLevelPacket {
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
