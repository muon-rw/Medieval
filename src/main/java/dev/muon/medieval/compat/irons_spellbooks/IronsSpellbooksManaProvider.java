package dev.muon.medieval.compat.irons_spellbooks;

import dev.muon.medieval.hotbar.ManaProvider;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.Minecraft;

public class IronsSpellbooksManaProvider implements ManaProvider {
    @Override
    public double getCurrentMana() {
        return ClientMagicData.getPlayerMana();
    }

    @Override
    public float getMaxMana() {
        if (Minecraft.getInstance().player == null) return 0;
        return (float) Minecraft.getInstance().player.getAttributeValue(AttributeRegistry.MAX_MANA.get());
    }

    @Override
    public float getReservedMana() {
        return 0; // Iron's Spellbooks doesn't have reserved mana concept
    }

    @Override
    public long getGameTime() {
        if (Minecraft.getInstance().level == null) return 0;
        return Minecraft.getInstance().level.getGameTime();
    }
}