package dev.muon.medieval.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public interface FTBHelper {
    BlockPos isAnyClaimed(ServerLevel level, BoundingBox boundingBox);
}