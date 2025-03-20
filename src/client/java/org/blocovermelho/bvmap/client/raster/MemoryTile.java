package org.blocovermelho.bvmap.client.raster;

import com.mojang.blaze3d.systems.RenderSystem;
import folk.sisby.surveyor.terrain.RegionSummary;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import org.blocovermelho.bvmap.MapMod;

import java.util.Optional;

/**
 * NIBT requires the render thread to work
 * This is an attempt in making a DynamicTile that can work on other threads
 */
public class MemoryTile {
    private static int WIDTH = 512;
    private static int HEIGHT = 512;

    public ChunkPos regionPos;
    private NativeImage pixels = new NativeImage(WIDTH, HEIGHT, false);
    private Identifier textureId;

    // Splash Color: 458 With 50% opacity
    public static int COLOR_UNLOADED = 0x7fe28d71;
    // Splash Color: 932 with 100% opacity;
    public static int COLOR_ERROR = 0xff3855ff;
    // Splash Color: 212 With 50% opacity
    public static int COLOR_EMPTY = 0x7f381c38;

    public MemoryTile(ChunkPos regionPos) {
        this.regionPos = regionPos;
        this.pixels.fillRect(0,0, WIDTH, HEIGHT, COLOR_EMPTY);
    }

    public void updateChunk(ChunkPos chunkPos, NativeImage raster) {
        assert chunkPos.getRegionX() == regionPos.x && chunkPos.getRegionZ() == regionPos.z;

        if (raster.getWidth() != 16 || raster.getHeight() != 16) {
            // Throw an error and blit with error texture.
            MapMod.LOGGER.error("[DynTile] Failure when trying to update ({}, {}). Raster was w={}xh={} instead of 16x16.", chunkPos.x, chunkPos.z, raster.getWidth(), raster.getHeight());
            return;
        }

        if (this.textureId != null) {
            RenderSystem.recordRenderCall(() -> {
                handleInline(chunkPos, raster);
            });
        } else {
            copyPixelData(chunkPos, raster, this.pixels);
        }
    }

    /**
     * Should only be called from the render thread
     * @return The ID of the newly submitted texture
     */
    public Identifier submit() {
        if (this.textureId == null) {
            // Note: NIBT now owns the handle to this object.
            NativeImageBackedTexture texture = new NativeImageBackedTexture(this.pixels);
            this.textureId = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("bvmap/region", texture);

            MapMod.LOGGER.info("|MemT| Submitted Tile: {}", this.textureId);
        }
        return this.textureId;
    }

    /**
     * @see #submit()
     * @return Returns the registered texture id.
     */
    public Optional<Identifier> getTextureId() {
        return Optional.ofNullable(textureId);
    }

    private void handleInline(ChunkPos chunkPos, NativeImage src) {
        NativeImageBackedTexture nibt = (NativeImageBackedTexture) MinecraftClient.getInstance().getTextureManager().getTexture(this.textureId);
        NativeImage dest = nibt.getImage();

        assert dest != null;
        copyPixelData(chunkPos, src, dest);
        nibt.upload();
    }

    private void copyPixelData(ChunkPos chunkPos, NativeImage src, NativeImage dest) {
        int relX = RegionSummary.regionRelative(chunkPos.x);
        int relY = RegionSummary.regionRelative(chunkPos.z);

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                // Copy bits from raster to tile
                int srcColor = src.getColor(x, y);
                dest.setColor(relX * 16 + x, relY * 16 + y, srcColor);
            }
        }
    }
}
