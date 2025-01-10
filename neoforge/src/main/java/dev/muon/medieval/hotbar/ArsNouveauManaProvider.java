package dev.muon.medieval.hotbar;

import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.client.ClientInfo;

public class ArsNouveauManaProvider implements ManaProvider {
    /**
     * Called via Mixin - See {@link dev.muon.medieval.mixin.compat.ars_nouveau.GuiManaHUDMixin}
     */
    private final IManaCap manaCap;

    public ArsNouveauManaProvider(IManaCap manaCap) {
        this.manaCap = manaCap;
    }

    @Override
    public double getCurrentMana() {
        return manaCap.getCurrentMana();
    }

    @Override
    public float getMaxMana() {
        return manaCap.getMaxMana();
    }

    @Override
    public float getReservedMana() {
        return ClientInfo.reservedOverlayMana;
    }

    @Override
    public long getGameTime() {
        return ClientInfo.ticksInGame;
    }
}