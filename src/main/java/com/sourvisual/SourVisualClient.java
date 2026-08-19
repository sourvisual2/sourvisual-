package com.sourvisual;

import com.sourvisual.config.SourVisualConfig;
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
        SourVisualConfig.load();

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

            handleCustomKeyBinds(client);
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) ->
                Watermark.render(drawContext, MinecraftClient.getInstance()));
    }

    private void handleCustomKeyBinds(MinecraftClient client) {
        if (client.currentScreen != null) {
            return;
        }
        if (client.player == null || client.getNetworkHandler() == null) {
            return;
        }
        long handle = client.getWindow().getHandle();

        for (SourVisualConfig.KeyBindEntry entry : SourVisualConfig.keyBinds) {
            if (entry.keyCode < 0) {
                continue;
            }
            boolean pressed = InputUtil.isKeyPressed(handle, entry.keyCode);
            if (pressed && !entry.pressedLastTick) {
                String command = entry.command.trim();
                if (command.startsWith("/")) {
                    command = command.substring(1);
                }
                if (!command.isEmpty()) {
                    client.getNetworkHandler().sendChatCommand(command);
                }
            }
            entry.pressedLastTick = pressed;
        }
    }
}
