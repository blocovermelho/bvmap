package org.blocovermelho.bvmap.client.event;

import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.client.SurveyorClientEvents;
import folk.sisby.surveyor.terrain.WorldTerrainSummary;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.blocovermelho.bvmap.MapMod;
import org.blocovermelho.bvmap.client.MapModClient;
import org.blocovermelho.bvmap.client.persistent.ScrappyImageCache;

import java.util.Collection;


public class ScrappyTerrainUpdated implements SurveyorClientEvents.TerrainUpdated {
    @Override
    public void onTerrainUpdated(World world, WorldTerrainSummary terrainSummary, Collection<ChunkPos> chunks) {

        if (MapModClient.CACHE == null) {
            MapModClient.CACHE = new ScrappyImageCache(WorldSummary.of(world), world.getHeight());
        }

        for (ChunkPos chunk : chunks) {
            MapMod.LOGGER.info("|STU| Updated Chunk: {}", chunk);
            MapModClient.CACHE.updateChunk(terrainSummary.get(chunk), chunk);
        }
    }
}
