package dev.muon.medieval.platform;

import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
import dev.ftb.mods.ftbchunks.api.ClaimedChunkManager;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class FTBHelperNeoForge implements FTBHelper {
    @Override
    public BlockPos isAnyClaimed(ServerLevel level, BoundingBox boundingBox) {
        ClaimedChunkManager manager = FTBChunksAPI.api().getManager();
        if (manager == null) {
            return null;
        }

        ChunkPos chunkPosMin = new ChunkPos(boundingBox.minX() >> 4, boundingBox.minZ() >> 4);
        ChunkPos chunkPosMax = new ChunkPos(boundingBox.maxX() >> 4, boundingBox.maxZ() >> 4);

        for (int x = chunkPosMin.x; x <= chunkPosMax.x; x++) {
            for (int z = chunkPosMin.z; z <= chunkPosMax.z; z++) {
                ClaimedChunk chunk = manager.getChunk(new ChunkDimPos(level.dimension(), x, z));
                if (chunk != null) {
                    return new BlockPos(x << 4, 0, z << 4);
                }
            }
        }

        return null;
    }
}