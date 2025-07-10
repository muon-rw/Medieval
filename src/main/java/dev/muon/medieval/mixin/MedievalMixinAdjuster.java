package dev.muon.medieval.mixin;

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode;
import com.bawnorton.mixinsquared.api.MixinAnnotationAdjuster;
import dev.muon.medieval.Medieval;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.List;
import java.util.Objects;

public class MedievalMixinAdjuster implements MixinAnnotationAdjuster {

    @Override
    public AdjustableAnnotationNode adjust(List<String> targetClassNames, String mixinClassName, MethodNode method, AdjustableAnnotationNode annotation) {
        if (!mixinClassName.equals("com.glisco.conjuring.mixin.LivingEntityMixin")) {
            return annotation;
        }

        if (annotation.is(Inject.class)) {
            String methodName = method.name;
            if (methodName.equals("calculateDamageReduction") || methodName.equals("applyDamageReduction") || methodName.equals("applySwordAoe")) {
                Medieval.LOGGER.info("Medieval Adjuster: Disabling @Inject in Conjuring mixin {}.{}", mixinClassName, methodName);
                return null;
            }
        }

        return annotation;
    }
}