package dev.muon.medieval.mixin.compat.irons_spellbooks;


import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastResult;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractSpell.class, remap = false)
public class AbstractSpellMixin {

    @ModifyReturnValue(method = "getManaCost", at = @At("RETURN"))
    private int adjustManaCost(int original) {
        return original * 2;
    }

    @ModifyReturnValue(method = "getSpellPower", at = @At("RETURN"))
    private float getSpellPower(float original, @Local(argsOnly = true) Entity sourceEntity){
        if (!(sourceEntity instanceof LivingEntity)) {
            return original;
        }
        double entitySpellPowerModifier = ((LivingEntity)sourceEntity).getAttributeValue(PerkAttributes.SPELL_DAMAGE_BONUS);
        return (float) (original * (entitySpellPowerModifier * 0.1 /*todo: config this value*/));
    }


    @Inject(method = "canBeCastedBy", at = @At(value = "RETURN", ordinal = 4))
    private void onManaError(int spellLevel, CastSource castSource, MagicData playerMagicData, Player player, CallbackInfoReturnable<CastResult> cir) {
        // TODO: Trigger text display on cast error, will need to sync to clients
    }
}
