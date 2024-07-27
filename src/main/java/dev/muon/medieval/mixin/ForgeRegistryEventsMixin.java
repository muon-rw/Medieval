package dev.muon.medieval.mixin;

import com.minecraftserverzone.mobhealthbar.GuiHelper;
import com.minecraftserverzone.mobhealthbar.ForgeRegistryEvents;
import com.mojang.blaze3d.vertex.PoseStack;
import daripher.autoleveling.event.MobsLevelingEvents;
import daripher.skilltree.capability.skill.IPlayerSkills;
import daripher.skilltree.capability.skill.PlayerSkillsProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderNameTagEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ForgeRegistryEvents.class, remap = false)
public class ForgeRegistryEventsMixin {
    /**
     * Might switch to this, modifying YDM's name rendering.
     * For now our own event works fine
     */
/*
    private static Entity capturedEntity;

    @Inject(method = "renderHpBar", at = @At("HEAD"))
    private static void captureEntity(RenderNameTagEvent event, CallbackInfo ci) {
        capturedEntity = event.getEntity();
    }

    @Redirect(
            method = "renderHpBar",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/minecraftserverzone/mobhealthbar/GuiHelper;drawString(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V",
                    ordinal = 0
            )
    )
    private static void redirectDrawString(PoseStack stack, Font font, String text, int x, int y, int color, boolean shadow) {
        if (capturedEntity instanceof LivingEntity) {
            int entityLevel = MobsLevelingEvents.getLevel((LivingEntity) capturedEntity);
            String modifiedText = text + " Level " + (entityLevel + 1);
            int modifiedColor = getLevelColor(Minecraft.getInstance().player, entityLevel);
            GuiHelper.drawString(stack, font, modifiedText, x, y, modifiedColor, shadow);
        } else {
            GuiHelper.drawString(stack, font, text, x, y, color, shadow);
        }
    }

    private static int getLevelColor(Player player, int entityLevel) {
        if (player == null || !PlayerSkillsProvider.hasSkills(player)) {
            return 0xFFFF00; // yellow
        }

        IPlayerSkills skills = PlayerSkillsProvider.get(player);
        int playerLevel = skills.getPlayerSkills().size();
        int levelDifference = entityLevel - playerLevel;

        if (levelDifference > 10) {
            return 0xFF0000; // red
        } else if (levelDifference > 0) {
            return 0xFFFF00; // yellow
        } else {
            return 0x00FF00; // green
        }
    }

 */
}