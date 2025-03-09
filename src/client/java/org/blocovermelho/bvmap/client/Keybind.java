package org.blocovermelho.bvmap.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.blocovermelho.bvmap.client.screen.ScrappyFiddleScreen;
import org.lwjgl.glfw.GLFW;

/**
 * Taken from
 * <a href="https://github.com/Abbie5/amap/blob/main/src/main/java/cc/abbie/amap/client/AMapKeybinds.java">AMap</a>
 * and <a href="https://wiki.fabricmc.net/tutorial:keybinds">Fabric Wiki</a>
 */
public class Keybind implements ClientTickEvents.EndTick {
    // We don't have a world map yet, and all these keybinds are subject to change.
    private static final String DEBUG_CATEGORY = "key.categories.bvmap.debug";
    public static final KeyBinding OPEN_DEBUG_SCREEN = new KeyBinding("key.bvmap.debug.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_KP_1, DEBUG_CATEGORY);

    public static void register() {
        KeyBindingHelper.registerKeyBinding(OPEN_DEBUG_SCREEN);
    }
    @Override
    public void onEndTick(MinecraftClient minecraftClient) {
        if (OPEN_DEBUG_SCREEN.wasPressed()) {
            minecraftClient.setScreen(new ScrappyFiddleScreen(minecraftClient.currentScreen));
        }
    }
}
