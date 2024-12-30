package dev.muon.medieval.mixin;

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode;
import com.bawnorton.mixinsquared.api.MixinAnnotationAdjuster;
import dev.muon.medieval.Medieval;
import org.spongepowered.asm.mixin.injection.Inject;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class MedievalMixinAdjuster implements MixinAnnotationAdjuster {
    @Override
    public AdjustableAnnotationNode adjust(List<String> targetClassNames, String mixinClassName, MethodNode method, AdjustableAnnotationNode annotation) {
        if (mixinClassName.equals("io.github.apace100.apoli.mixin.ItemStackMixin")
                && method.name.equals("copyNewParams")
                && annotation.is(Inject.class)) {
            Medieval.LOGGER.warn("Disabling Apoli's ItemStack#copyNewParams. " +
                    "This may cause issues if you are not using this mod inside of Medieval MC or Otherworld!");
            return null;
        }
        return annotation;
    }
}