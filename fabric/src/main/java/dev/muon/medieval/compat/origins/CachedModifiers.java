package dev.muon.medieval.compat.origins;

import io.github.apace100.apoli.util.modifier.Modifier;

import java.util.List;

public class CachedModifiers {
    public List<Modifier> powerModifiers;
    public double lastBaseValue;
    public int lastModifierHash;

    public boolean isValid(double baseValue, int modifierHash) {
        return lastBaseValue == baseValue && lastModifierHash == modifierHash;
    }
}
