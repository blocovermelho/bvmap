package org.blocovermelho.bvmap.client.screen;

import com.google.common.base.Stopwatch;
import com.mojang.blaze3d.systems.RenderSystem;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.terrain.RegionSummary;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.blocovermelho.bvmap.MapMod;
import org.blocovermelho.bvmap.client.MapModClient;
import org.blocovermelho.bvmap.client.raster.AlbedoRasterizer;
import org.blocovermelho.bvmap.client.raster.DynamicTile;

import java.util.HashMap;

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

    HashMap<ChunkPos, DynamicTile> tiles = new HashMap<>();

    long lastMillis = 0;
    Stopwatch stopwatch = Stopwatch.createUnstarted();

    /// Screen Space
    ChunkPos s_CenterPixel = ChunkPos.ORIGIN;

    ///  World Space
    ChunkPos w_Origin = ChunkPos.ORIGIN;

    public ScrappyFiddleScreen(Screen parent) {
        super(Text.literal("BVMap - Dev Pre-Alpha"));
        MapMod.LOGGER.info("ScrappyFiddleScreen Created");
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.s_CenterPixel = new ChunkPos(width / 2 , height / 2);

        if (client != null && client.player != null) {
            ChunkPos chunkPos = client.player.getChunkPos();
            ChunkPos regionPos = new ChunkPos(RegionSummary.chunkToRegion(chunkPos.x), RegionSummary.chunkToRegion(chunkPos.z));
            this.w_Origin = new ChunkPos(client.player.getBlockX(), client.player.getBlockZ());

            // Its 2013 and you're programing java 1.8
            // There's likely a better way to do this but do I care? nah.
            if (!MapModClient.EXPLORED_REGIONS.contains(regionPos)) {
                MapModClient.EXPLORED_REGIONS.add(regionPos);
            }

            World world = client.player.getWorld();
            WorldSummary worldSummary = WorldSummary.of(world);

            for (ChunkPos region : MapModClient.EXPLORED_REGIONS) {
                if (tiles.containsKey(region)) continue;
                stopwatch.reset();
                stopwatch.start();
                var raster =  AlbedoRasterizer.transformRegion(region, worldSummary, world.getHeight());
                stopwatch.stop();
                this.lastMillis += stopwatch.elapsed().toMillis();
                tiles.put(region, raster);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // TODO: Fix Transparency. It works only for the last tile that was rendered.
        RenderSystem.enableBlend();

        tiles.forEach((k, v) -> {
            var pos = this.worldToScreen(new ChunkPos(k.x * 512, k.z * 512));

            context.drawTexture(v.getResourceKey(),
                    pos.x, pos.z,
                    512,512,
                    0, 0,
                    512, 512,
                    512,512
            );

            context.drawCenteredTextWithShadow(textRenderer, "x=" + k.x + ", z=" + k.z, pos.x + 256, pos.z + 256, 0xffffffff);
        });

        RenderSystem.disableBlend();

        context.drawText(textRenderer,
                Text.of("Rasterized " + tiles.size() + " region(s) in AVG: " + lastMillis / tiles.size() + " ms. Total: "  + lastMillis + " ms.")
                , mouseX + 8 , mouseY, 0xffffffff, true);
    }

    /**
     * Converts a screen space coordinate to a world coordinate
     * @param screen The screen space coordinate
     * @return The world space coordinate
     */
    public ChunkPos screenToWorld(ChunkPos screen) {
        // We need to account for the center pixel position being 0,0
        int x = screen.x - this.s_CenterPixel.x;
        int z = screen.z - this.s_CenterPixel.z;

        // Add the current world origin to get real positions
        x = x + this.w_Origin.x;
        z = z + this.w_Origin.z;

        return new ChunkPos(x,z);
    }

    /**
     * Converts a world space coordinate to a screen coordinate
     * @param world The world space coordinate (<em>Absolute</em> Block Position)
     * @return The screen space coordinate (Pixel Position)
     */
    public ChunkPos worldToScreen(ChunkPos world) {
        // Translate based on the current world origin
        int x = world.x - this.w_Origin.x;
        int z = world.z - this.w_Origin.z;

        // Moving towards the center of screen
        x = this.s_CenterPixel.x + x;
        z = this.s_CenterPixel.z + z;

        return new ChunkPos(x,z);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
