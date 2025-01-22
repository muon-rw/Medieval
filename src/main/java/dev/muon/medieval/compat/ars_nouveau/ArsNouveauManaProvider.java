package dev.muon.medieval.compat.ars_nouveau;

import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.client.ClientInfo;
import dev.muon.medieval.hotbar.ManaProvider;

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