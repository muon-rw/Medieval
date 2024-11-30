package dev.muon.medieval.mixin.compat.skilltree;

import daripher.skilltree.network.message.LearnSkillMessage;
import dev.muon.medieval.leveling.LevelSyncHandler;
import dev.muon.medieval.leveling.LevelingUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(value = LearnSkillMessage.class, remap = false)
public class LearnSkillMessageMixin {
    @Inject(method = "receive", at = @At("TAIL"))
    private static void onSkillLearned(LearnSkillMessage message, Supplier<NetworkEvent.Context> ctxSupplier, CallbackInfo ci) {
        ServerPlayer player = ctxSupplier.get().getSender();
        if (player != null) {
            int newLevel = LevelingUtils.getPlayerLevel(player);
            LevelSyncHandler.INSTANCE.send(
                    PacketDistributor.ALL.noArg(),
                    new LevelSyncHandler.SyncPlayerLevelPacket(player.getUUID(), newLevel)
            );
        }
    }
}