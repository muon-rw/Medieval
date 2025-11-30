package dev.muon.medieval.mixin.compat.apothic_compats;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Attention onlookers:
 * Do not do this
 * Don't tell Shadows either
 * ok thanks
 */
@Mixin(value = DynamicRegistry.class, remap = false)
public class DynamicRegistryMixin {

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("Medieval/DynamicRegistryMixin");

    @Shadow
    @Final
    protected String path;

    @Unique
    private static final Set<String> DISABLED_CATEGORIES = Set.of(
        "apothic_compats:bracelet",
        "apothic_compats:body",
        "apothic_compats:belt",
        "apothic_compats:charm",
        "apothic_compats:back",
        "apothic_compats:head",
        "apothic_compats:feet",
        "apothic_compats:curio"
    );

    @Unique
    private static final Set<String> REGISTRIES_TO_FILTER = Set.of(
        "extra_gem_bonuses",
        "affixes",
        "affix_loot_entries"
    );

    @Inject(
        method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
        at = @At("HEAD")
    )
    private void medieval$filterDisabledCategories(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        if (!REGISTRIES_TO_FILTER.contains(this.path)) {
            return;
        }

        LOGGER.debug("[Medieval] Filtering disabled loot categories from {} registry ({} entries)", this.path, objects.size());

        Iterator<Map.Entry<ResourceLocation, JsonElement>> mapIterator = objects.entrySet().iterator();
        while (mapIterator.hasNext()) {
            Map.Entry<ResourceLocation, JsonElement> entry = mapIterator.next();
            JsonElement element = entry.getValue();
            
            if (!element.isJsonObject()) {
                continue;
            }
            
            JsonObject json = element.getAsJsonObject();
            
            if ("extra_gem_bonuses".equals(this.path)) {
                int removed = medieval$filterBonusesArray(json, "bonuses");
                if (removed > 0) {
                    LOGGER.debug("[Medieval] Filtered {} invalid bonuses from {}", removed, entry.getKey());
                }
                if (json.has("bonuses") && json.getAsJsonArray("bonuses").isEmpty()) {
                    LOGGER.debug("[Medieval] Removing {} (no valid bonuses remaining)", entry.getKey());
                    mapIterator.remove();
                }
            } else if ("affixes".equals(this.path)) {
                int removed = medieval$filterAffixCategories(json);
                if (removed > 0) {
                    LOGGER.debug("[Medieval] Filtered {} invalid categories from affix {}", removed, entry.getKey());
                }
                if (json.has("categories") && json.getAsJsonArray("categories").isEmpty()) {
                    LOGGER.debug("[Medieval] Removing affix {} (no valid categories remaining)", entry.getKey());
                    mapIterator.remove();
                }
            } else if ("affix_loot_entries".equals(this.path)) {
                if (medieval$hasDisabledLootCategory(json)) {
                    LOGGER.debug("[Medieval] Removing affix_loot_entry {} (disabled category)", entry.getKey());
                    mapIterator.remove();
                }
            }
        }
    }

    @Unique
    private static int medieval$filterBonusesArray(JsonObject json, String arrayKey) {
        if (!json.has(arrayKey)) {
            return 0;
        }

        JsonElement bonusesElement = json.get(arrayKey);
        if (!bonusesElement.isJsonArray()) {
            return 0;
        }

        JsonArray bonuses = bonusesElement.getAsJsonArray();
        Iterator<JsonElement> iterator = bonuses.iterator();
        int removed = 0;
        
        while (iterator.hasNext()) {
            JsonElement bonusElement = iterator.next();
            if (bonusElement.isJsonObject()) {
                JsonObject bonus = bonusElement.getAsJsonObject();
                if (medieval$shouldRemoveBonus(bonus)) {
                    iterator.remove();
                    removed++;
                }
            }
        }
        return removed;
    }

    @Unique
    private static int medieval$filterAffixCategories(JsonObject json) {
        // Affixes have a "categories" array at the top level
        if (!json.has("categories")) {
            return 0;
        }

        JsonElement categoriesElement = json.get("categories");
        if (!categoriesElement.isJsonArray()) {
            return 0;
        }

        JsonArray categories = categoriesElement.getAsJsonArray();
        Iterator<JsonElement> iterator = categories.iterator();
        int removed = 0;
        
        while (iterator.hasNext()) {
            JsonElement catElement = iterator.next();
            if (catElement.isJsonPrimitive()) {
                String category = catElement.getAsString();
                if (medieval$isDisabledCategory(category)) {
                    iterator.remove();
                    removed++;
                }
            }
        }
        return removed;
    }

    @Unique
    private static boolean medieval$shouldRemoveBonus(JsonObject bonus) {
        // gem_class can be either a string (single category) or an object with "types" array
        if (!bonus.has("gem_class")) {
            return false;
        }

        JsonElement gemClass = bonus.get("gem_class");
        
        if (gemClass.isJsonPrimitive()) {
            // Simple string reference like "apothic_compats:curio"
            String category = gemClass.getAsString();
            boolean shouldRemove = medieval$isDisabledCategory(category);
            if (shouldRemove) {
                LOGGER.debug("[Medieval] Removing bonus with gem_class: {}", category);
            }
            return shouldRemove;
        } else if (gemClass.isJsonObject()) {
            // Explicit format: { "key": "...", "types": [...] }
            JsonObject gemClassObj = gemClass.getAsJsonObject();
            if (gemClassObj.has("types")) {
                JsonElement types = gemClassObj.get("types");
                if (types.isJsonArray()) {
                    JsonArray typesArray = types.getAsJsonArray();
                    
                    // First, filter out disabled types from the array
                    Iterator<JsonElement> typeIterator = typesArray.iterator();
                    int removedTypes = 0;
                    while (typeIterator.hasNext()) {
                        JsonElement type = typeIterator.next();
                        if (type.isJsonPrimitive() && medieval$isDisabledCategory(type.getAsString())) {
                            LOGGER.debug("[Medieval] Removing type {} from gem_class types array", type.getAsString());
                            typeIterator.remove();
                            removedTypes++;
                        }
                    }
                    
                    // If all types were removed, remove the whole bonus
                    if (typesArray.isEmpty()) {
                        LOGGER.debug("[Medieval] All types removed from bonus, removing entire bonus");
                        return true;
                    }
                    
                    if (removedTypes > 0) {
                        LOGGER.debug("[Medieval] Filtered {} types, {} remaining", removedTypes, typesArray.size());
                    }
                }
            }
        }
        
        return false;
    }

    @Unique
    private static boolean medieval$isDisabledCategory(String category) {
        // Handle both full ResourceLocation and path-only formats
        if (DISABLED_CATEGORIES.contains(category)) {
            return true;
        }
        // Also check without namespace for legacy format
        for (String disabled : DISABLED_CATEGORIES) {
            if (disabled.endsWith(":" + category)) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private static boolean medieval$hasDisabledLootCategory(JsonObject json) {
        String[] categoryFields = {"loot_category", "category", "type"};
        
        for (String field : categoryFields) {
            if (json.has(field)) {
                JsonElement element = json.get(field);
                if (element.isJsonPrimitive()) {
                    String value = element.getAsString();
                    if (medieval$isDisabledCategory(value)) {
                        return true;
                    }
                }
            }
        }

        if (json.has("categories")) {
            JsonElement categoriesElement = json.get("categories");
            if (categoriesElement.isJsonArray()) {
                JsonArray categories = categoriesElement.getAsJsonArray();
                if (categories.isEmpty()) {
                    return false;
                }
                boolean allDisabled = true;
                for (JsonElement cat : categories) {
                    if (cat.isJsonPrimitive() && !medieval$isDisabledCategory(cat.getAsString())) {
                        allDisabled = false;
                        break;
                    }
                }
                return allDisabled;
            }
        }
        
        return false;
    }
}
