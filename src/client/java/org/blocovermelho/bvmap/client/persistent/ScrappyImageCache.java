package org.blocovermelho.bvmap.client.persistent;

import com.mojang.blaze3d.systems.RenderSystem;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.terrain.ChunkSummary;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.blocovermelho.bvmap.client.raster.AlbedoRasterizer;
import org.blocovermelho.bvmap.client.raster.DynamicTile;
import org.blocovermelho.bvmap.client.raster.MemoryTile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Barebones caching solution
 */
public class ScrappyImageCache {
    private HashMap<ChunkPos, MemoryTile> tileset = new HashMap<>();
    private ConcurrentHashMap<ChunkPos, Identifier> dirty = new ConcurrentHashMap<>();
    private WorldSummary summary;
    private int height;

    public ScrappyImageCache(WorldSummary summary, int worldHeight) {
        this.summary = summary;
        this.height = worldHeight;
    }

    public MemoryTile getTileAt(ChunkPos regionPos) {
        if (!tileset.containsKey(regionPos)) {
            tileset.put(regionPos, AlbedoRasterizer.transformRegion(regionPos, this.summary, this.height));
        }
        return tileset.get(regionPos);
    }

    public List<MemoryTile> getTilesFor(BlockPos blockTopLeft, BlockPos blockBottomRight) {
        ChunkPos c_topleft = new ChunkPos(blockTopLeft);
        ChunkPos c_bottomright = new ChunkPos(blockBottomRight);

        ChunkPos r_topleft = new ChunkPos(c_topleft.getRegionX(), c_topleft.getRegionZ());
        ChunkPos r_bottomright = new ChunkPos (c_bottomright.getCenterX(), c_bottomright.getCenterZ());

        List<MemoryTile> tiles = new ArrayList<>();

        for (int x = r_topleft.x; x <= r_bottomright.x; x++) {
            for (int z = r_topleft.z; z <= r_bottomright.z; z++) {
                ChunkPos region = new ChunkPos(x, z);
                if (tileset.containsKey(region)) {
                    tiles.add(tileset.get(region));
                }
            }
        }

        return tiles;
    }

    public void updateChunk(ChunkSummary summary, ChunkPos chunkPos) {
        var regionPos = new ChunkPos(chunkPos.getRegionX(), chunkPos.getRegionZ());
        assert this.summary.terrain() != null;
        var regionSummary = this.summary.terrain().getRegion(regionPos);
        var tile = this.getTileAt(regionPos);
        var raster = AlbedoRasterizer.transformChunk(summary, regionSummary.getBlockPalette(), this.height);

        tile.updateChunk(chunkPos, raster);

        if (tile.getTextureId().isPresent()) {
            dirty.putIfAbsent(tile.regionPos, tile.getTextureId().get());
        }
    }

    /*

    public Optional<Identifier> getDirtyTextureAt(ChunkPos regionPos) {
        return Optional.ofNullable(dirty.get(regionPos));
    }

    public void removeDirtyTextureAt(ChunkPos regionPos) {
        dirty.remove(regionPos);
    }
     */
}
