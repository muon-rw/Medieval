package dev.muon.medieval.mixin.compat.apothic_compats;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apothic_attributes.modifiers.EntitySlotGroup;
import ianm1647.apothic_compats.loot.ModLootCategories;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;
import java.util.function.Predicate;

@Mixin(value = ModLootCategories.class, remap = false)
public class ModLootCategoriesMixin {

    @Shadow
    private static LootCategory register(String path, Predicate<ItemStack> filter, EntitySlotGroup slots) {
        throw new IllegalStateException();
    }

    @Unique
    private static final Set<String> DISABLED_CURIO_SLOTS = Set.of(
        "bracelet",
        "body", 
        "belt",
        "charm",
        "back",
        "head",
        "feet",
        "curio"
    );
    
    @WrapOperation(
            method = "registerLootCategories",
            at = @At(
                    value = "INVOKE",
                    target = "Lianm1647/apothic_compats/loot/ModLootCategories;register(Ljava/lang/String;Ljava/util/function/Predicate;Ldev/shadowsoffire/apothic_attributes/modifiers/EntitySlotGroup;)Ldev/shadowsoffire/apotheosis/loot/LootCategory;"
            )
    )
    private static LootCategory blacklistCurioAffixes(String path, Predicate<ItemStack> filter, EntitySlotGroup slots, Operation<LootCategory> original) {
        if (DISABLED_CURIO_SLOTS.contains(path)) {
            return null;
        }
        return original.call(path, filter, slots);
    }
}