package org.blocovermelho.bvmap.client;

import folk.sisby.surveyor.WorldSummary;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.math.ChunkPos;

public class MapModClient implements ClientModInitializer {
    public static ObjectOpenHashSet<ChunkPos> EXPLORED_REGIONS = new ObjectOpenHashSet<>();

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(new Keybind());
        Keybind.register();

        WorldSummary.enableTerrain();
        WorldSummary.enableLandmarks();
    }
}
