package com.sourvisual;

import com.sourvisual.gui.VisualScreen;
import com.sourvisual.hud.Watermark;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SourVisualClient implements ClientModInitializer {

    private static KeyBinding toggleMenuKey;

    @Override
    public void onInitializeClient() {
        toggleMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.sourvisual.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "key.categories.sourvisual"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleMenuKey.wasPressed()) {
                if (client.currentScreen instanceof VisualScreen) {
                    client.setScreen(null);
                } else if (client.currentScreen == null) {
                    client.setScreen(new VisualScreen());
                }
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) ->
                Watermark.render(drawContext, MinecraftClient.getInstance()));
    }
}
