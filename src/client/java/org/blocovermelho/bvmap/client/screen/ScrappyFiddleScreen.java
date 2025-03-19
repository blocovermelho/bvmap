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
import org.joml.Vector2i;

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

    // Constants
    public static final int NATIVE_IMAGE_SIZE = 512;
    public static final float SCROLL_PER_TICK = 1.125f;

    // Scaling
    /// relative blocks / relative pixels
    private float blocksPerPixel = 1;

    /// Absolute Screen-Space Coordinate
    Vector2i as_CenterPixel = new Vector2i(0,0);

    /// Absolute World-Space Pixel
    ChunkPos aw_Origin = ChunkPos.ORIGIN;

    public ScrappyFiddleScreen(Screen parent) {
        super(Text.literal("BVMap - Dev Pre-Alpha"));
        MapMod.LOGGER.info("ScrappyFiddleScreen Created");
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.as_CenterPixel = new Vector2i(width / 2 , height / 2);

        if (client != null && client.player != null) {
            ChunkPos chunkPos = client.player.getChunkPos();
            ChunkPos regionPos = new ChunkPos(RegionSummary.chunkToRegion(chunkPos.x), RegionSummary.chunkToRegion(chunkPos.z));
            this.aw_Origin = new ChunkPos(client.player.getBlockX(), client.player.getBlockZ());

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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Experimentation from playing with google maps:
        // - Once zoomed, the irl coordinates remains at the same pixel position
        // - The closer something is to the point being zoomed, the less it moves

        double scrolls = Math.pow(SCROLL_PER_TICK, -verticalAmount);
        double newScale = this.blocksPerPixel * scrolls;
        this.blocksPerPixel = (float) NATIVE_IMAGE_SIZE / Math.round(NATIVE_IMAGE_SIZE / newScale);

        ChunkPos a = this.screenToWorld(new Vector2i((int) Math.round(mouseX), (int) Math.round(mouseY)));

        int dz = (int) Math.round((a.z - this.aw_Origin.z) * (1 - scrolls));
        int dx = (int) Math.round((a.x - this.aw_Origin.x) * (1 - scrolls));

        this.aw_Origin = new ChunkPos(this.aw_Origin.x + dx, this.aw_Origin.z + dz);

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int b_deltaX = (int) Math.round(this.blocksPerPixel * deltaX);
        int b_deltaY = (int) Math.round(this.blocksPerPixel * deltaY);

        this.aw_Origin = new ChunkPos(this.aw_Origin.x - b_deltaX, this.aw_Origin.z - b_deltaY);

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        RenderSystem.enableBlend();

        tiles.forEach((k, v) -> {
            var pos = this.worldToScreen(new ChunkPos(k.x * NATIVE_IMAGE_SIZE, k.z * NATIVE_IMAGE_SIZE));

            context.drawTexture(v.getResourceKey(),
                    pos.x, pos.y, // Screen Top Left
                    Math.round(512 / this.blocksPerPixel), Math.round(512 / this.blocksPerPixel),  // Paint Size
                    0, 0, // Texture Top Left
                    NATIVE_IMAGE_SIZE,NATIVE_IMAGE_SIZE, // Texture Read Size
                    NATIVE_IMAGE_SIZE,NATIVE_IMAGE_SIZE // Texture Total Size
            );

//            context.drawCenteredTextWithShadow(textRenderer, "x=" + k.x + ", z=" + k.z, pos.x + 256, pos.z + 256, 0xffffffff);
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
    public ChunkPos screenToWorld(Vector2i screen) {
        // We need to account for the center pixel position being 0,0
        int rs_x = screen.x - this.as_CenterPixel.x;
        int rs_y = screen.y - this.as_CenterPixel.y;

        // Relative Pixel * (Relative Block / Relative Pixel) = Relative Blocks
        int rw_scaledX = (int) (rs_x * this.blocksPerPixel);
        int rw_scaledZ = (int) (rs_y * this.blocksPerPixel);

        // Add the current world origin to get real positions
        int world_x = rw_scaledX + this.aw_Origin.x;
        int world_z = rw_scaledZ + this.aw_Origin.z;

        return new ChunkPos(world_x, world_z);
    }

    /**
     * Converts a world space coordinate to a screen coordinate
     * @param world The world space coordinate (<em>Absolute</em> Block Position)
     * @return The screen space coordinate (Pixel Position)
     */
    public Vector2i worldToScreen(ChunkPos world) {
        // Translate based on the current world origin
        int rw_x = world.x - this.aw_Origin.x;
        int rw_z = world.z - this.aw_Origin.z;

        // Relative Block / (Relative Block / Relative Pixel) = Relative Pixel
        int rs_scaledx = Math.round(rw_x / this.blocksPerPixel);
        int rs_scaledz = Math.round(rw_z / this.blocksPerPixel);

        // Moving towards the center of screen
        int screen_x = this.as_CenterPixel.x + rs_scaledx;
        int screen_y = this.as_CenterPixel.y + rs_scaledz;

        return new Vector2i(screen_x, screen_y);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
