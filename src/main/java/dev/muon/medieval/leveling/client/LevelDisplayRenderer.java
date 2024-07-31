package dev.muon.medieval.leveling.client;

import com.minecraftserverzone.mobhealthbar.GuiHelper;
import com.minecraftserverzone.mobhealthbar.configs.ConfigHolder;
import com.minecraftserverzone.mobhealthbar.configs.HpBarModConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import daripher.autoleveling.event.MobsLevelingEvents;
import daripher.skilltree.capability.skill.IPlayerSkills;
import daripher.skilltree.capability.skill.PlayerSkillsProvider;
import dev.muon.medieval.Medieval;
import net.blay09.mods.waystones.core.Waystone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
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

@Mod.EventBusSubscriber(modid = Medieval.MODID, value = Dist.CLIENT)
public class LevelDisplayRenderer {
    private static final float TEXT_SCALE = -0.02F;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity entity = (LivingEntity) event.getEntity();
        int entityLevel = MobsLevelingEvents.getLevel(entity) + 1;

        if (entityLevel > 0 && shouldRender(entity)) {
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;

            String entityName = event.getContent().getString();
            String levelText = " Level " + (entityLevel);
            String combinedText = entityName + levelText;

            event.setContent(Component.literal(combinedText));

            PoseStack matrixStack = event.getPoseStack();
            Font font = event.getEntityRenderer().getFont();

            int textWidth = font.width(combinedText);
            float f2 = -textWidth / 2.0f;

            matrixStack.pushPose();
            matrixStack.translate(0, entity.getBbHeight() + 1.058F, 0);
            matrixStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
            matrixStack.scale(TEXT_SCALE, TEXT_SCALE, TEXT_SCALE);

            int posXAdd = HpBarModConfig.HP_BAR_TYPE[1].get();
            int posYAdd = HpBarModConfig.HP_BAR_TYPE[2].get();

            int levelColor = getLevelColor(player, entityLevel);

            GuiHelper.drawString(matrixStack, font, entityName, (int)f2 + posXAdd, posYAdd, 0xFFFFFF, false);
            GuiHelper.drawString(matrixStack, font, levelText, (int)f2 + posXAdd + font.width(entityName), posYAdd, levelColor, false);

            matrixStack.popPose();

            event.setResult(RenderNameTagEvent.Result.ALLOW);
        } else {
            event.setResult(RenderNameTagEvent.Result.DEFAULT);
        }
    }

    private static boolean shouldRender(LivingEntity entity) {
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

    private static int getLevelColor(Player player, int entityLevel) {
        if (!PlayerSkillsProvider.hasSkills(player)) {
            return 0xFFFF00; //yellow
        }

        IPlayerSkills skills = PlayerSkillsProvider.get(player);
        int playerLevel = skills.getPlayerSkills().size();

        int levelDifference = entityLevel - playerLevel;

        if (levelDifference > 10) {
            return 0xFF0000; // red
        } else if (levelDifference > -5) {
            return 0xFFFF00; // yellow
        } else {
            return 0x00FF00; // green
        }
    }
}