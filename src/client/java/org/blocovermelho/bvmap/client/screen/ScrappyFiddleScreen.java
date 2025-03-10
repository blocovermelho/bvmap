package org.blocovermelho.bvmap.client.screen;

import com.google.common.base.Stopwatch;
import com.mojang.blaze3d.systems.RenderSystem;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.terrain.RegionSummary;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.blocovermelho.bvmap.MapMod;
import org.blocovermelho.bvmap.client.raster.AlbedoRasterizer;
import org.blocovermelho.bvmap.client.raster.DynamicTile;

/**
 * This is no place of honor
 * No highly esteemed deed is commemorated here
 * This is the scrappy fiddle:tm:
 * It is temporary, broken and filled with bad code
 * The importance of this is to get things going and documented
 * and for us to not copy code without understanding what it does.
 */

@Environment(EnvType.CLIENT)
public class ScrappyFiddleScreen extends Screen {
    private final Screen parent;

    DynamicTile tile = null;
    long lastMillis = -1;

    int originX = 10;
    int originY = 10;

    public ScrappyFiddleScreen(Screen parent) {
        super(Text.literal("BVMap - Dev Pre-Alpha"));
        MapMod.LOGGER.info("ScrappyFiddleScreen Created");
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.originX = width / 2;
        this.originY = height / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // This is horrifyingly bad, and absolutely no one should do it like this
        // I'm only doing it this way since it's the most *dumb* way to get this done for testing
        // Currently this renders only one tile (the current one) once, when the user clicks for the first time
        // I'll have to think of a tiling solution for tiling my `DynamicTile`s
        // And some sort of caching system to make them live for more than a single screen.
        // Also, transparency is broken.

        if (this.tile == null) {
            if (client != null) {
                ClientPlayerEntity player = client.player;
                if (player != null) {
                    ChunkPos chunkPos = player.getChunkPos();
                    ChunkPos regionPos = new ChunkPos(RegionSummary.chunkToRegion(chunkPos.x), RegionSummary.chunkToRegion(chunkPos.z));

                    World world = player.getWorld();
                    WorldSummary worldSummary = WorldSummary.of(world);
                    Stopwatch stopwatch = Stopwatch.createStarted();
                    this.tile = AlbedoRasterizer.transformRegion(regionPos, worldSummary, world.getHeight());
                    stopwatch.stop();
                    this.lastMillis = stopwatch.elapsed().toMillis();
                }
            }
        }

        if (this.tile != null) {
            // X, Y: Top Left Coordinates
            // Width, Height: Size of Screen
            // U, V: Top Left Texture Coordinates
            // Region {Width, Height}: Size of the Region to be fetched
            // Texture {Width, Height}: Real Texture Size
            RenderSystem.enableBlend();
            context.drawTexture(this.tile.getResourceKey(),
                    originX - 256, originY - 256,
                    512, 512,
                    0, 0,
                    512, 512,
                    512, 512
            );
            RenderSystem.disableBlend();
        }

        context.drawText(textRenderer, Text.of("Rasterized 1 region in " + lastMillis + " ms"), mouseX + 8 , mouseY, 0xffffffff, true);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
