package dev.muon.medieval.mixin.compat.ars_nouveau;

import com.hollingsworth.arsnouveau.common.block.tile.CraftingLecternTile;
import daripher.itemproduction.block.entity.Interactive;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = CraftingLecternTile.class, remap = false)
public class CraftingLecternTileInteractiveMixin implements Interactive {
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