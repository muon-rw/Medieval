package dev.muon.medieval.mixin.compat.irons_spellbooks;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.muon.medieval.attribute.CurioAttributeHandler;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CurioBaseItem.class)
public abstract class CurioBaseItemMixin{

    @ModifyReturnValue(method = "getAttributeModifiers", at = @At("RETURN"))
    private Multimap<Holder<Attribute>, AttributeModifier> remapCurioAttributes(Multimap<Holder<Attribute>, AttributeModifier> original) {
        return CurioAttributeHandler.remapCurioAttributes(original);
    }
}