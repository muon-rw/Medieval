package dev.muon.medieval.leveling;

import dev.muon.medieval.Medieval;
import net.minecraft.nbt.IntTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

//@Mod.EventBusSubscriber(modid = Medieval.MODID)
public class OriginalLevelCapability {
    public static final Capability<OriginalLevel> ORIGINAL_LEVEL = CapabilityManager.get(new CapabilityToken<>(){});
    private static final ResourceLocation CAPABILITY_ID = new ResourceLocation(Medieval.MODID, "original_level");

    //@SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(CAPABILITY_ID, new OriginalLevelProvider());
        }
    }

    public static class OriginalLevel {
        private int level = -1;

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            if (this.level == -1) {
                this.level = level;
            }
        }
    }

    private static class OriginalLevelProvider implements ICapabilitySerializable<IntTag> {
        private final LazyOptional<OriginalLevel> instance = LazyOptional.of(OriginalLevel::new);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return ORIGINAL_LEVEL.orEmpty(cap, instance);
        }

        @Override
        public IntTag serializeNBT() {
            return IntTag.valueOf(instance.orElseThrow(() -> new IllegalStateException("LazyOptional must not be empty!")).getLevel());
        }

        @Override
        public void deserializeNBT(IntTag nbt) {
            instance.orElseThrow(() -> new IllegalStateException("LazyOptional must not be empty!")).setLevel(nbt.getAsInt());
        }
    }
}