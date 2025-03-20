package org.blocovermelho.bvmap.client.raster;

import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.terrain.ChunkSummary;
import folk.sisby.surveyor.terrain.RegionSummary;
import folk.sisby.surveyor.util.RegistryPalette;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.math.ChunkPos;
import org.blocovermelho.bvmap.MapMod;

public class AlbedoRasterizer {
    public static MemoryTile transformRegion(ChunkPos regionPos, WorldSummary worldSummary, int worldHeight) {
        MapMod.LOGGER.info("[AlbedoRasterizer] Transforming Region ({}, {})", regionPos.x, regionPos.z);
        // NOTE: This currently has no caching and just makes the dynamic tiles on the spot
        // Which makes them not quite "dynamic". In a sense so far the mod is "pure" and keeps no state
        // regenerating things once the user presses a key.
        // This *has* to change later, unless I've somehow came up with decent code first try. (Unlikely).
        MemoryTile tile = new MemoryTile(regionPos);
        RegionSummary regionSummary =  worldSummary.terrain().getRegion(regionPos);
        MapMod.LOGGER.info("[AlbedoRasterizer] Got summary for ({}, {})? {}", regionPos.x, regionPos.z , regionSummary != null);

        RegistryPalette<Block>.ValueView blockPalette = regionSummary.getBlockPalette();

        for (int x = 0; x < 32; x++) {
            for (int z = 0; z < 32; z++) {
                ChunkPos chunkPos = new ChunkPos(32 * regionPos.x + x, 32 * regionPos.z + z);
                ChunkSummary chunkSummary = regionSummary.get(chunkPos);
                if (chunkSummary == null) {
                    continue;
                }

                var image = transformChunk(chunkSummary, blockPalette, worldHeight);
                tile.updateChunk(chunkPos, image);
            }
        }

        return tile;
    }

    public static NativeImage transformChunk(ChunkSummary chunk, RegistryPalette<Block>.ValueView blockPallete, int worldHeight) {
        NativeImage inner = new NativeImage(16,16,false);
        inner.fillRect(0,0, 16,16, DynamicTile.COLOR_UNLOADED);

        var topmostLayer = chunk.toSingleLayer(null, null, worldHeight);
        // It was an empty chunk.
        if (topmostLayer == null) {
            inner.fillRect(0,0, 16,16, DynamicTile.COLOR_EMPTY);
            return inner;
        }

        int[] blocks = topmostLayer.blocks();

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                int idx = x * 16 + y; // The 2d plane got flattened into a single dimensional array.
                Block b = blockPallete.get(blocks[idx]);
                int color = b != null ? b.getDefaultMapColor().getRenderColor(MapColor.Brightness.NORMAL)
                        : DynamicTile.COLOR_UNLOADED;
                inner.setColor(x,y, color);
            }
        }


        return inner;
    }
}
