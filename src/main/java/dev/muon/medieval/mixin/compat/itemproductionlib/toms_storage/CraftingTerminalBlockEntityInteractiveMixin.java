package dev.muon.medieval.mixin.compat.itemproductionlib.toms_storage;

import com.tom.storagemod.tile.CraftingTerminalBlockEntity;
import daripher.itemproduction.block.entity.Interactive;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = CraftingTerminalBlockEntity.class,remap = false)
public class CraftingTerminalBlockEntityInteractiveMixin implements Interactive {
    @Unique
    private Player lastUser;

    @Override
    public void setUser(Player player) {
        this.lastUser = player;
    }

    @Override
    public Player getUser() {
        return this.lastUser;
    }
}