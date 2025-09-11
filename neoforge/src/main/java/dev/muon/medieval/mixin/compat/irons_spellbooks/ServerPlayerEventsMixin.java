package dev.muon.medieval.mixin.compat.irons_spellbooks;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.muon.medieval.Medieval;
import io.redspace.ironsspellbooks.api.item.UpgradeData;
import io.redspace.ironsspellbooks.player.ServerPlayerEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ServerPlayerEvents.class, remap = false)
public abstract class ServerPlayerEventsMixin {

    @WrapOperation(method = "handleUpgradeModifiers",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EquipmentSlotGroup;bySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/entity/EquipmentSlotGroup;"))
    private static EquipmentSlotGroup catchNullFallback(EquipmentSlot slot, Operation<EquipmentSlotGroup> original, @Local(argsOnly = true) ItemAttributeModifierEvent event) {
        if (slot == null) {
            Medieval.LOG.warn("Had to return ANY for invalid slot for item {}", event.getItemStack().getHoverName());
            return EquipmentSlotGroup.ANY;
        }
        return original.call(slot);
    }

    @WrapMethod(method = "handleUpgradeModifiers")
    private static void catchNullSlot(ItemAttributeModifierEvent event, Operation<Void> original) {
        UpgradeData upgradeData = UpgradeData.getUpgradeData(event.getItemStack());
        if (upgradeData != UpgradeData.NONE) {
            if (event.getItemStack().getEquipmentSlot() == null) {
                // Do nothing, let Curio handler deal with this item
            } else {
                original.call(event);
            }
        }
    }
}
