package dev.muon.medieval;

import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.util.List;
import java.util.Set;

public class MixinConfigPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {

        if (mixinClassName.contains(".compat.")) {
            String[] parts = mixinClassName.split("\\.");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("compat")) {
                    if (i + 2 < parts.length && parts[i + 1].equals("itemproductionlib")) {
                        String modId = parts[i + 2];
                        return isModLoaded("itemproductionlib") && isModLoaded(modId);
                    } else if (i + 1 < parts.length) {
                        String modId = parts[i + 1];
                        return isModLoaded(modId);
                    }
                }
            }
        }


        if (mixinClassName.contains("TreasureGoblinBonusMixin")) {
            return isModLoaded("apotheosis") && isModLoaded("dummmmmmy");
        }

        return true;
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