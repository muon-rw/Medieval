package dev.muon.medieval.mixin.compat.skill_tree_rpgs;


import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.SkillsAPI;
import net.skill_tree_rpgs.utils.SkillHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(SkillHelper.class)
public class SkillHelperMixin {
    @Inject(method = "respec", at = @At("HEAD"), cancellable = true)
    private static void modifyRespec(ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
        boolean anyReset = SkillsAPI.streamUnlockedCategories(player)
                .filter(category -> category.getSpentPoints(player) > 0)
                .peek(category -> category.resetSkills(player))
                .findAny()
                .isPresent();
        
        // Return true if any categories were reset, false otherwise
        cir.setReturnValue(anyReset);
        cir.cancel();
    }
}
