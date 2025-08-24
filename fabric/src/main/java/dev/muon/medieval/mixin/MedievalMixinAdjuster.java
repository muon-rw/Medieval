package dev.muon.medieval.mixin;

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode;
import com.bawnorton.mixinsquared.api.MixinAnnotationAdjuster;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class MedievalMixinAdjuster implements MixinAnnotationAdjuster {

    @Override
    public AdjustableAnnotationNode adjust(List<String> targetClassNames, String mixinClassName, MethodNode method, AdjustableAnnotationNode annotation) {
        if (mixinClassName.equals("io.github.apace100.apoli.mixin.EntityAttributeInstanceMixin")) {
            if (method.name.equals("apoli$modifyAttribute") && annotation.is(ModifyReturnValue.class)) {
                return null;
            }
        }

        return annotation;
    }
}