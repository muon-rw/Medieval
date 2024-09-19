package dev.muon.medieval.item;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ChallengeOrbItemNeoForge extends ChallengeOrbItem {
    private static DeferredHolder<DataComponentType<?>, DataComponentType<ChallengeOrbData>> CHALLENGE_ORB_DATA;

    public ChallengeOrbItemNeoForge(Properties properties) {
        super(properties);
    }

    @Override
    protected ChallengeOrbData getData(ItemStack stack) {
        return stack.getOrDefault(CHALLENGE_ORB_DATA.value(), new ChallengeOrbData(null, 0));
    }

    @Override
    protected void setData(ItemStack stack, ChallengeOrbData data) {
        stack.set(CHALLENGE_ORB_DATA.value(), data);
    }

    public static void registerDataComponent(DeferredRegister.DataComponents register) {
        CHALLENGE_ORB_DATA = register.registerComponentType("challenge_orb_data",
                builder -> builder
                        .persistent(ChallengeOrbData.CODEC)
                        .cacheEncoding()
        );
    }
}