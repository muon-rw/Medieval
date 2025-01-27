package dev.muon.medieval.item;

import dev.muon.medieval.config.MedievalConfig;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.ArrayList;
import net.minecraft.world.entity.item.ItemEntity;
import java.util.Collection;

@Mod.EventBusSubscriber
public class LootLimiter {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingDrops(LivingDropsEvent event) {
        Collection<ItemEntity> drops = event.getDrops();
        int totalStacks = 0;

        for (ItemEntity item : drops) {
            totalStacks++;
        }

        if (totalStacks > MedievalConfig.get().maxStacksPerEntity) {
            ArrayList<ItemEntity> limitedDrops = new ArrayList<>();
            int currentStacks = 0;

            for (ItemEntity item : new ArrayList<>(drops)) {
                if (currentStacks < MedievalConfig.get().maxStacksPerEntity) {
                    limitedDrops.add(item);
                    currentStacks++;
                }
            }

            drops.clear();
            drops.addAll(limitedDrops);
        }
    }
}