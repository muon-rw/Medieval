package dev.muon.medieval.network;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TownPortalScrollPacket {
    private final PacketType type;
    private final double remainingSeconds;

    public enum PacketType {
        UPDATE_COUNTDOWN,
        INTERRUPT,
        COMPLETE
    }

    public TownPortalScrollPacket(PacketType type, double remainingSeconds) {
        this.type = type;
        this.remainingSeconds = remainingSeconds;
    }

    public static void encode(TownPortalScrollPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.type);
        buffer.writeDouble(packet.remainingSeconds);
    }

    public static TownPortalScrollPacket decode(FriendlyByteBuf buffer) {
        return new TownPortalScrollPacket(
                buffer.readEnum(PacketType.class),
                buffer.readDouble()
        );
    }

    public static void handle(TownPortalScrollPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                player = net.minecraft.client.Minecraft.getInstance().player;
                switch (packet.type) {
                    case UPDATE_COUNTDOWN -> {
                        String formattedTime = String.format("%.1f", packet.remainingSeconds);
                        player.displayClientMessage(Component.literal("Channeling: " + formattedTime + "s"), true);
                    }
                    case INTERRUPT -> {
                        player.displayClientMessage(Component.translatable("item.medieval.town_portal_scroll.interrupted")
                                .withStyle(ChatFormatting.RED), true);
                    }
                    case COMPLETE -> {
                        player.displayClientMessage(Component.translatable("item.medieval.town_portal_scroll.success")
                                .withStyle(ChatFormatting.GREEN), true);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}