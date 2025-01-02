package dev.muon.medieval.mixin;

import com.bawnorton.mixinsquared.api.MixinCanceller;
import dev.muon.medieval.Medieval;

import java.util.List;

public class MedievalMixinCanceller implements MixinCanceller {
    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        if (mixinClassName.equals("net.dehydration.mixin.PotionItemMixin")) {
            Medieval.LOG.info("Disabled Dehydration PotionItemMixin");
            return true;
        }
        return false;
    }
}