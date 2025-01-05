package dev.muon.medieval.mixin.compat.irons_spellbooks;

import com.hollingsworth.arsnouveau.api.client.IDisplayMana;
import io.redspace.ironsspellbooks.item.CastingItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CastingItem.class)
public class CastingItemMixin implements IDisplayMana {
}
