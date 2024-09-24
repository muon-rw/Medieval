package dev.muon.medieval.mixin.compat.justlevelingfork;

import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import dev.muon.medieval.leveling.event.AptitudeChangedEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AptitudeCapability.class, remap = false)
public class AptitudeCapabilityMixin {

    @Inject(method = "setAptitudeLevel", at = @At("RETURN"))
    private void onSetAptitudeLevel(Aptitude aptitude, int lvl, CallbackInfo ci) {
        AptitudeCapability self = (AptitudeCapability) (Object) this;
        Player player = medieval$getPlayerFromCapability(self);
        if (player != null) {
            int oldLevel = self.getAptitudeLevel(aptitude);
            MinecraftForge.EVENT_BUS.post(new AptitudeChangedEvent(player, aptitude.getName(), oldLevel, lvl));
        }
    }

    @Inject(method = "addAptitudeLevel", at = @At("RETURN"))
    private void onAddAptitudeLevel(Aptitude aptitude, int addLvl, CallbackInfo ci) {
        AptitudeCapability self = (AptitudeCapability) (Object) this;
        Player player = medieval$getPlayerFromCapability(self);
        if (player != null) {
            int newLevel = self.getAptitudeLevel(aptitude);
            int oldLevel = newLevel - addLvl;
            MinecraftForge.EVENT_BUS.post(new AptitudeChangedEvent(player, aptitude.getName(), oldLevel, newLevel));
        }
    }

    @Unique
    private Player medieval$getPlayerFromCapability(AptitudeCapability capability) {
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            for (ServerLevel level : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
                for (Player player : level.players()) {
                    LazyOptional<AptitudeCapability> cap = player.getCapability(com.seniors.justlevelingfork.registry.RegistryCapabilities.APTITUDE);
                    if (cap.isPresent() && cap.resolve().get() == capability) {
                        return player;
                    }
                }
            }
        }
        return null;
    }
}