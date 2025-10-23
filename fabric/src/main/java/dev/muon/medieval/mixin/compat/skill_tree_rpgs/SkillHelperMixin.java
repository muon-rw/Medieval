package dev.muon.medieval.mixin.compat.skill_tree_rpgs;


import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.SkillsAPI;
import net.skill_tree_rpgs.utils.SkillHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;


@Mixin(SkillHelper.class)
public class SkillHelperMixin {
    @Inject(method = "respec", at = @At("HEAD"), cancellable = true)
    private static void modifyRespec(ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
        List<Category> categoriesToReset = SkillsAPI.streamUnlockedCategories(player)
                .filter(category -> category.getSpentPoints(player) > 0)
                .toList();

        categoriesToReset.forEach(category -> category.resetSkills(player));
        cir.setReturnValue(!categoriesToReset.isEmpty());
        cir.cancel();
    }
}
