package dev.muon.medieval;

import com.bawnorton.mixinsquared.adjuster.MixinAnnotationAdjusterRegistrar;
import com.bawnorton.mixinsquared.canceller.MixinCancellerRegistrar;
import dev.muon.medieval.mixin.MedievalMixinAdjuster;
import dev.muon.medieval.mixin.MedievalMixinCanceller;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinConfigPluginFabric implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
        MixinCancellerRegistrar.register(new MedievalMixinCanceller());
        MixinAnnotationAdjusterRegistrar.register(new MedievalMixinAdjuster());
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {

        if (mixinClassName.contains(".compat.")) {
            String[] parts = mixinClassName.split("\\.");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("compat") && i + 1 < parts.length) {
                    String modId = parts[i + 1];
                    return FabricLoader.getInstance().isModLoaded(modId);
                }
            }
            // This means there was a failure in parsing the mod id
            return false;
        }
        return true;
    }


    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

}