package dev.muon.medieval.mixin.compat.irons_spellbooks;

import com.hollingsworth.arsnouveau.common.capability.ManaCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.muon.medieval.Medieval;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MagicData.class)
public class MagicDataMixin {
    @Shadow
    private ServerPlayer serverPlayer;

    @WrapMethod(method = "getMana", remap = false)
    public float redirectGetMana(Operation<Float> original) {
        if (this.serverPlayer == null) {
            return original.call();
        }
        ManaCap manaCap = CapabilityRegistry.getMana(this.serverPlayer);

        if (manaCap != null) {
            return (float) manaCap.getCurrentMana();
        }

        Medieval.LOG.warn("Tried to get {}'s Mana but couldn't get their Mana Cap!", serverPlayer.getName());

        return original.call();
    }

    @WrapMethod(method = "setMana", remap = false)
    public void redirectSetMana(float mana, Operation<Void> original) {
        if (this.serverPlayer == null) {
            original.call(mana);
            return;
        }
        ManaCap manaCap = CapabilityRegistry.getMana(this.serverPlayer);
        if (manaCap != null) {

            manaCap.setMana(mana);
            return;
        }

        Medieval.LOG.warn("Tried to set {}'s Mana but couldn't get their Mana Cap!", serverPlayer.getName());
    }

}