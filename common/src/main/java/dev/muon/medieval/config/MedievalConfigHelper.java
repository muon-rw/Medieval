package dev.muon.medieval.config;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface MedievalConfigHelper {
    List<String> getAllowedStructureNamespaces();
    List<String> getAdditionalValidStructures();
    boolean isValidStructure(ResourceLocation structureId);
}