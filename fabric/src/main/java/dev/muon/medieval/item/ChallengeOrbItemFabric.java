package dev.muon.medieval.item;

import dev.muon.medieval.Medieval;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;

public class ChallengeOrbItemFabric extends ChallengeOrbItem {
    public static DataComponentType<ChallengeOrbData> CHALLENGE_ORB_DATA;

    public ChallengeOrbItemFabric(Properties properties) {
        super(properties);
    }

    @Override
    protected ChallengeOrbData getData(ItemStack stack) {
        return stack.getOrDefault(CHALLENGE_ORB_DATA, new ChallengeOrbData("", 0));
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
                        .networkSynchronized(ByteBufCodecs.fromCodec(ChallengeOrbData.CODEC))
                        .build()
        );
    }
}