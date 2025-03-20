package org.blocovermelho.bvmap.client.event;

import com.google.common.collect.Multimap;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.client.SurveyorClientEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.structure.Structure;
import org.blocovermelho.bvmap.client.MapModClient;
import org.blocovermelho.bvmap.client.persistent.ScrappyImageCache;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class ScrappyWorldLoad implements SurveyorClientEvents.WorldLoad {
    @Override
    public void onWorldLoad(ClientWorld world, WorldSummary summary, ClientPlayerEntity player, Map<ChunkPos, BitSet> terrain, Multimap<RegistryKey<Structure>, ChunkPos> structures, Multimap<UUID, Identifier> landmarks) {
        MapModClient.CACHE = new ScrappyImageCache(summary, world.getHeight());
        HashSet<ChunkPos> exploredRegions = new HashSet<>();

        for (ChunkPos chunkPos : terrain.keySet()) {
            var region = new ChunkPos(chunkPos.getRegionX(), chunkPos.getRegionZ());
            if (exploredRegions.contains(region)) continue;
            MapModClient.CACHE.getTileAt(region);
            exploredRegions.add(region);
        }

    }
}
