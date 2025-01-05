package dev.muon.medieval.attribute;

import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

import java.util.HashMap;
import java.util.Map;

public class AttributeRemapper {
    private static final Map<Holder<Attribute>, AttributeMapping> MAPPINGS = new HashMap<>();

    static {
        if (isModLoaded("ars_nouveau") && (isModLoaded("irons_spellbooks"))) {
            register(AttributeRegistry.MAX_MANA, PerkAttributes.MAX_MANA, 1.0);
            register(AttributeRegistry.MANA_REGEN, PerkAttributes.MANA_REGEN_BONUS, 0.6);
            register(PerkAttributes.SPELL_DAMAGE_BONUS, AttributeRegistry.SPELL_POWER, 0.1);
        }
    }

    private static boolean isModLoaded(String modId) {
        if (ModList.get() == null) {
            return LoadingModList.get().getMods().stream().map(ModInfo::getModId).anyMatch(modId::equals);
        }
        return ModList.get().isLoaded(modId);
    }

    private record AttributeMapping(Holder<Attribute> target, double conversionRate) {
    }

    private static void register(Holder<Attribute> source, Holder<Attribute> target, double conversionRate) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Source or target Holder cannot be null during registration.");
        }
        MAPPINGS.put(source, new AttributeMapping(target, conversionRate));
    }

    public static Holder<Attribute> getRemappedHolder(Holder<Attribute> holder) {
        AttributeMapping mapping = MAPPINGS.get(holder);
        return mapping != null ? mapping.target() : holder;
    }

    public static double getConvertedValue(Holder<Attribute> source, double value) {
        AttributeMapping mapping = MAPPINGS.get(source);
        return mapping != null ? value * mapping.conversionRate() : value;
    }
}