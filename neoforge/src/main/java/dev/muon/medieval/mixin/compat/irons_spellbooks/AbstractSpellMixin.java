package dev.muon.medieval.mixin.compat.irons_spellbooks;


import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.muon.medieval.ManaTextHelper;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastResult;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractSpell.class, remap = false)
public class AbstractSpellMixin {

    @WrapMethod(method = "getManaCost")
    public int getManaCost(int level, Operation<Integer> original) {
        return 2 * original.call(level);
    }

    // TODO: Implement on dedicated servers
    @Inject(method = "canBeCastedBy", at = @At(value = "RETURN", ordinal = 4))
    private void onManaError(int spellLevel, CastSource castSource, MagicData playerMagicData, Player player, CallbackInfoReturnable<CastResult> cir) {
        ManaTextHelper.onManaError();
    }
}
