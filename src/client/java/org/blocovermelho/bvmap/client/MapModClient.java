package org.blocovermelho.bvmap.client;

import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.client.SurveyorClientEvents;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.math.ChunkPos;
import org.blocovermelho.bvmap.MapMod;
import org.blocovermelho.bvmap.client.event.ScrappyTerrainUpdated;
import org.blocovermelho.bvmap.client.event.ScrappyWorldLoad;
import org.blocovermelho.bvmap.client.persistent.ScrappyImageCache;

public class MapModClient implements ClientModInitializer {
    public static ObjectOpenHashSet<ChunkPos> EXPLORED_REGIONS = new ObjectOpenHashSet<>();
    public static ScrappyImageCache CACHE;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(new Keybind());
        Keybind.register();

        SurveyorClientEvents.Register.worldLoad(MapMod.id("scrappy_world_load"), new ScrappyWorldLoad());
        SurveyorClientEvents.Register.terrainUpdated(MapMod.id("scrappy_terrain_updated"), new ScrappyTerrainUpdated());

        WorldSummary.enableTerrain();
        WorldSummary.enableLandmarks();
    }
}
