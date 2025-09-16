package dev.muon.medieval;

import com.bawnorton.mixinsquared.adjuster.MixinAnnotationAdjusterRegistrar;
import dev.muon.medieval.mixin.MedievalMixinAdjuster;
import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class MixinConfigPlugin implements IMixinConfigPlugin {
    
    private static final Logger LOGGER = LogManager.getLogger("Medieval-MixinConfig");

    @Override
    public void onLoad(String mixinPackage) {
        MixinAnnotationAdjusterRegistrar.register(new MedievalMixinAdjuster());
        LOGGER.info("Medieval Mixin Config Plugin loaded");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains(".compat.")) {

            // Special cases first
            if (mixinClassName.contains("ManaBarOverlayMixin")) {
                boolean ironsLoaded = isModLoaded("irons_spellbooks");
                boolean arsLoaded = isModLoaded("ars_nouveau");
                boolean shouldApply = ironsLoaded && !arsLoaded;
                
                if (!shouldApply) {
                    if (!ironsLoaded) {
                        LOGGER.info("Disabling mixin {} because required mod 'irons_spellbooks' is not loaded", 
                            getSimpleMixinName(mixinClassName));
                    } else if (arsLoaded) {
                        LOGGER.info("Disabling mixin {} because 'ars_nouveau' is loaded",
                            getSimpleMixinName(mixinClassName));
                    }
                }
                return shouldApply;
            }


            // Standard handling:
            // Each subdirectory within /compat/ is a modid
            String[] parts = mixinClassName.split("\\.");
            List<String> requiredMods = new ArrayList<>();
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("compat")) {
                    // Collect all mod IDs in the path after "compat" until the class name
                    for (int j = i + 1; j < parts.length - 1; j++) { // -1 to exclude the class name
                        requiredMods.add(parts[j]);
                    }
                    break;
                }
            }
            for (String modId : requiredMods) {
                if (!isModLoaded(modId)) {
                    LOGGER.info("Disabling mixin {} because required mod '{}' is not loaded", 
                        getSimpleMixinName(mixinClassName), modId);
                    return false;
                }
            }
            if (!requiredMods.isEmpty()) {
                LOGGER.info("Enabling mixin {} - all required mods {} are loaded",
                    getSimpleMixinName(mixinClassName), requiredMods);
            }
            return true;
        }

        return true;
    }
    
    private String getSimpleMixinName(String mixinClassName) {
        // Extract just the class name from the full package path
        String[] parts = mixinClassName.split("\\.");
        return parts[parts.length - 1];
    }

    private static boolean isModLoaded(String modId) {
        if (ModList.get() == null) {
            return LoadingModList.get().getMods().stream().map(ModInfo::getModId).anyMatch(modId::equals);
        }
        return ModList.get().isLoaded(modId);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}