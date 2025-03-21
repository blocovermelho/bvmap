package org.blocovermelho.bvmap;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapMod implements ModInitializer {
    public static final String ID = "bvmap";
    public static Logger LOGGER = LoggerFactory.getLogger(ID);

    @Override
    public void onInitialize() {
    }

    public static Identifier id(String path) {
        return Identifier.of(ID, path);
    }
}
