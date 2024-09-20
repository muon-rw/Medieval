package dev.muon.medieval.leveling;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import daripher.autoleveling.settings.EntityLevelingSettings;
import dev.muon.medieval.Medieval;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class EnhancedEntityLevelingSettingsReloader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = Deserializers.createLootTableSerializer().create();
    private static final Map<ResourceLocation, EnhancedEntityLevelingSettings> ENHANCED_SETTINGS = new HashMap<>();

    public EnhancedEntityLevelingSettingsReloader() {
        super(GSON, "leveling_settings/entities");
    }

    @Nullable
    public static EnhancedEntityLevelingSettings getSettingsForEntity(EntityType<?> entityType) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        EnhancedEntityLevelingSettings settings = ENHANCED_SETTINGS.get(entityId);
        if (settings != null) {
            Medieval.LOGGER.debug("Found enhanced settings for entity: {}", entityId);
        }
        return settings;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Medieval.LOGGER.debug("Loading enhanced entity leveling settings");
        ENHANCED_SETTINGS.clear();
        map.forEach(this::loadSettings);
        Medieval.LOGGER.debug("Loaded {} enhanced entity leveling settings", ENHANCED_SETTINGS.size());
    }

    private void loadSettings(ResourceLocation fileId, JsonElement jsonElement) {
        try {
            Medieval.LOGGER.debug("Loading enhanced leveling settings from file: {}", fileId);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Load original settings
            EntityLevelingSettings originalSettings = EntityLevelingSettings.load(jsonObject);

            // Load our enhanced settings
            Map<Attribute, AttributeModifier> attributeModifiers = readAttributeModifiers(jsonObject);

            EnhancedEntityLevelingSettings enhancedSettings = new EnhancedEntityLevelingSettings(originalSettings, attributeModifiers);

            // Extract the entity name from the file path
            String path = fileId.getPath();
            String[] pathParts = path.split("/");
            String entityName = pathParts[pathParts.length - 1].replace(".json", "");
            ResourceLocation entityId = new ResourceLocation(fileId.getNamespace(), entityName);

            ENHANCED_SETTINGS.put(entityId, enhancedSettings);

            Medieval.LOGGER.debug("Loaded enhanced settings for entity: {}", entityId);
        } catch (Exception exception) {
            Medieval.LOGGER.error("Couldn't parse enhanced leveling settings {}", fileId, exception);
        }
    }

    private Map<Attribute, AttributeModifier> readAttributeModifiers(JsonObject json) {
        Map<Attribute, AttributeModifier> attributeModifiers = new HashMap<>();
        if (json.has("attribute_modifiers")) {
            JsonArray modifiersArray = json.getAsJsonArray("attribute_modifiers");
            for (JsonElement element : modifiersArray) {
                JsonObject modifierObject = element.getAsJsonObject();
                String attributeKey = modifierObject.get("attribute").getAsString();
                ResourceLocation attributeId = new ResourceLocation(attributeKey);
                Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(attributeId);
                if (attribute != null) {
                    double amount = modifierObject.get("amount").getAsDouble();
                    AttributeModifier.Operation operation = getOperation(modifierObject.get("operation").getAsInt());
                    attributeModifiers.put(attribute, new AttributeModifier("LevelingBonus", amount, operation));
                    Medieval.LOGGER.debug("Added attribute modifier: {} = {} ({})", attributeId, amount, operation);
                } else {
                    Medieval.LOGGER.debug("Unknown attribute: {}", attributeId);
                }
            }
        }
        return attributeModifiers;
    }

    private AttributeModifier.Operation getOperation(int operationId) {
        return switch (operationId) {
            case 0 -> AttributeModifier.Operation.ADDITION;
            case 1 -> AttributeModifier.Operation.MULTIPLY_BASE;
            case 2 -> AttributeModifier.Operation.MULTIPLY_TOTAL;
            default -> {
                Medieval.LOGGER.debug("Unknown operation ID: {}. Defaulting to ADDITION", operationId);
                yield AttributeModifier.Operation.ADDITION;
            }
        };
    }
}