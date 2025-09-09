package dev.muon.medieval.leveling.client;

import com.minecraftserverzone.mobhealthbar.configs.ConfigHolder;
import daripher.autoleveling.event.MobsLevelingEvents;
import dev.muon.medieval.Medieval;
import dev.muon.medieval.leveling.LevelingUtils;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import net.mehvahdjukaar.dummmmmmy.common.TargetDummyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Medieval.MODID, value = Dist.CLIENT)
public class LevelDisplayRenderer {
    private static final float TEXT_SCALE = -0.02F;
    private static final Map<UUID, Integer> playerLevels = new HashMap<>();


    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            // event.setResult(RenderNameTagEvent.Result.DENY);
            return;
        }

        if (!shouldRender(entity)) {
            // event.setResult(RenderNameTagEvent.Result.DEFAULT);
            return;
        }

        int entityLevel = getEntityLevel(entity);
        if (entityLevel <= 0) {
            // event.setResult(RenderNameTagEvent.Result.DEFAULT);
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            // event.setResult(RenderNameTagEvent.Result.DEFAULT);
            return;
        }

        Component baseNameComponent = event.getContent();
        String baseNameString = baseNameComponent.getString();

        if (baseNameString.trim().isEmpty() && !(entity instanceof Player)) {
            baseNameComponent = entity.getName();
        }

        Component levelTextComponent = Component.literal(" Level " + entityLevel)
                .withStyle(style -> style.withColor(getLevelColor(player, entity)));

        MutableComponent newContent = baseNameComponent.copy().append(levelTextComponent);

        event.setContent(newContent);
        event.setResult(RenderNameTagEvent.Result.ALLOW);
    }

    private static int getEntityLevel(LivingEntity entity) {
        if (entity instanceof Player) {
            return playerLevels.getOrDefault(entity.getUUID(), 0);
        } else {
            return LevelingUtils.getEntityLevel(entity);
        }
    }

    public static void updatePlayerLevel(UUID playerId, int level) {
        playerLevels.put(playerId, level);
    }


    private static boolean shouldRender(LivingEntity entity) {
        if (!MobsLevelingEvents.shouldShowLevel(entity) || entity instanceof AntiMagicSusceptible || entity instanceof TargetDummyEntity) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return false;

        double distanceSq = player.distanceToSqr(entity);
        double renderDistance = ConfigHolder.COMMON.HP_BAR_TYPE[3].get();
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 reachVec = eyePos.add(lookVec.x * renderDistance, lookVec.y * renderDistance, lookVec.z * renderDistance);

        AABB boundingBox = entity.getBoundingBox().inflate(0.5);
        EntityHitResult result = boundingBox.clip(eyePos, reachVec).map(hit -> new EntityHitResult(entity, hit)).orElse(null);

        return (result != null && result.getEntity() == entity) && distanceSq <= renderDistance;
    }

    private static int getLevelColor(Player player, LivingEntity entity) {
        int playerLevel = getEntityLevel(player);
        int entityLevel = getEntityLevel(entity);

        if (playerLevel > 0) {
            int levelDifference = entityLevel - playerLevel;

            if (levelDifference > 10) {
                return 0xFF0000; // red
            } else if (levelDifference > -5) {
                return 0xFFFF00; // yellow
            } else {
                return 0x00FF00; // green
            }
        } else {
            if (entityLevel < 8) {
                return 0x00FF00; // green
            } else if (entityLevel <= 19) {
                return 0xFFFF00; // yellow
            } else {
                return 0xFF0000; // red
            }
        }
    }

}