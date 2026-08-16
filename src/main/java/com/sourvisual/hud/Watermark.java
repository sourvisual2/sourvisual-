package com.sourvisual.hud;

import com.sourvisual.config.SourVisualConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Watermark {

    private static final Identifier FONT = Identifier.of("sourvisual", "league_spartan");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Последние отрисованные границы — нужны для хит-теста драга в VisualScreen
    public static int lastX, lastY, lastW, lastH;

    public static void render(DrawContext ctx, MinecraftClient client) {
        if (client.player == null) return;

        StringBuilder sb = new StringBuilder();

        if (SourVisualConfig.wmLogo) {
            sb.append("sour visual");
        }

        if (SourVisualConfig.wmNickname) {
            appendSep(sb);
            sb.append(client.getSession().getUsername());
        }

        if (SourVisualConfig.wmFps) {
            appendSep(sb);
            sb.append(client.getCurrentFps()).append(" fps");
            // Если у тебя другой mappings-геттер FPS, замени client.getCurrentFps()
        }

        if (SourVisualConfig.wmPing) {
            appendSep(sb);
            sb.append(getPlayerPing(client)).append(" ms");
        }

        if (SourVisualConfig.wmServer) {
            appendSep(sb);
            sb.append(getServerName(client));
        }

        if (SourVisualConfig.wmTitle) {
            appendSep(sb);
            sb.append(LocalTime.now().format(TIME_FMT));
        }

        String line = sb.toString();
        if (line.isEmpty()) return;

        int x = SourVisualConfig.wmX;
        int y = SourVisualConfig.wmY;
        int w = client.textRenderer.getWidth(line) + 8;
        int h = client.textRenderer.fontHeight + 6;

        lastX = x;
        lastY = y;
        lastW = w;
        lastH = h;

        ctx.fill(x, y, x + w, y + h, 0x80000000);
        ctx.drawText(client.textRenderer,
                Text.literal(line).setStyle(Text.empty().getStyle().withFont(FONT)),
                x + 4, y + 3, 0xFFFFFFFF, true);
    }

    private static void appendSep(StringBuilder sb) {
        if (!sb.isEmpty()) sb.append("  |  ");
    }

    private static int getPlayerPing(MinecraftClient client) {
        if (client.player == null || client.getNetworkHandler() == null) return 0;
        var entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        return entry != null ? entry.getLatency() : 0;
    }

    private static String getServerName(MinecraftClient client) {
        if (client.getCurrentServerEntry() != null) {
            return client.getCurrentServerEntry().address;
        }
        return client.isIntegratedServerRunning() ? "singleplayer" : "unknown";
    }
    }
