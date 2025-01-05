package dev.muon.medieval.mixin.compat.irons_spellbooks;


import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractSpell.class)
public class AbstractSpellMixin {
    public AbstractSpellMixin() {
    }

    @WrapMethod(
            method = "getManaCost",
            remap = false
    )
    public int getManaCost(int level, Operation<Integer> original) {
        return 2 * original.call(level);
    }
}
