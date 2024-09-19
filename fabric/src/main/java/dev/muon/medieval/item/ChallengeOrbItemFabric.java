package dev.muon.medieval.item;

import dev.muon.medieval.Medieval;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ChallengeOrbItemFabric extends ChallengeOrbItem {
    private static DataComponentType<ChallengeOrbData> CHALLENGE_ORB_DATA;

    public ChallengeOrbItemFabric(Properties properties) {
        super(properties);
    }

    @Override
    protected ChallengeOrbData getData(ItemStack stack) {
        return stack.getOrDefault(CHALLENGE_ORB_DATA, new ChallengeOrbData(null, 0));
    }

    @Override
    protected void setData(ItemStack stack, ChallengeOrbData data) {
        stack.set(CHALLENGE_ORB_DATA, data);
    }

    public static void registerDataComponent() {
        CHALLENGE_ORB_DATA = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                Medieval.loc("challenge_orb_data"),
                DataComponentType.<ChallengeOrbData>builder()
                        .persistent(ChallengeOrbData.CODEC)
                        .build()
        );
    }
}