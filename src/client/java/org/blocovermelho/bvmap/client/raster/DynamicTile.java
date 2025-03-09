package org.blocovermelho.bvmap.client.raster;

import folk.sisby.surveyor.terrain.RegionSummary;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import org.blocovermelho.bvmap.MapMod;

/**
 * Represents a rasterized tile of a minecraft region
 */
public class DynamicTile implements AutoCloseable {
    // Splash Color: 458 With 50% opacity
    // Note: Colors usually are RGBA, but this is little-endian because of Java.
    // So we have to flip the bits around to be ABGR.
    public static int COLOR_UNLOADED = 0x7fe28d71;
    // Splash Color: 932 with 100% opacity;
    public static int COLOR_ERROR = 0xff3855ff;
    // Splash Color: 212
    public static int COLOR_EMPTY = 0x7f381c38;

    private static int WIDTH = 512;
    private static int HEIGHT = 512;

    private ChunkPos regionPos;
    private Identifier resourceKey;

    public DynamicTile(ChunkPos regionPos) {
        this.regionPos = regionPos;
        NativeImageBackedTexture texture = new NativeImageBackedTexture(WIDTH, HEIGHT, false);
        texture.getImage().fillRect(0,0, WIDTH , HEIGHT, COLOR_UNLOADED);
        this.resourceKey = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("bvmap/region", texture);
    }

    public void updateChunk(ChunkPos chunkPos, NativeImage raster){
        int relX = RegionSummary.regionRelative(chunkPos.x);
        int relY = RegionSummary.regionRelative(chunkPos.z);

        NativeImageBackedTexture texture = (NativeImageBackedTexture) MinecraftClient.getInstance().getTextureManager().getTexture(resourceKey);
        NativeImage image = texture.getImage();

        if (image == null) {
            MapMod.LOGGER.error("[DynTile] Failure when getting image from texture manager for ({}, {})", chunkPos.x, chunkPos.z);
            return;
        }

        if (raster.getWidth() != 16 || raster.getHeight() != 16) {
            // Throw an error and blit with error texture.
            MapMod.LOGGER.error("[DynTile] Failure when trying to update ({}, {}). Raster was w={}xh={} instead of 16x16.", chunkPos.x, chunkPos.z, raster.getWidth(), raster.getHeight());
            image.fillRect(relX * 16, relY * 16 , 16, 16, COLOR_ERROR);
        } else {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    // Copy bits from raster to tile
                    int srcColor = raster.getColor(x, y);
                    image.setColor(relX * 16 + x, relY * 16 + y, srcColor);
                }
            }
        }

        texture.upload();
    };

    public Identifier getResourceKey() {
        return resourceKey;
    }

    @Override
    public void close() throws Exception {
        MinecraftClient.getInstance().getTextureManager().destroyTexture(this.resourceKey);
    }
}
