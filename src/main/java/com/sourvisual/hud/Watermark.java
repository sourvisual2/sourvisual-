package com.sourvisual.hud;

import com.sourvisual.config.SourVisualConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Watermark {

    private static final Identifier FONT = Identifier.of("sourvisual", "vcr_osd_mono");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Последние отрисованные границы — нужны для хит-теста драга в VisualScreen
    public static int lastX, lastY, lastW, lastH;

    public static void render(DrawContext ctx, MinecraftClient client) {
        if (client.player == null) return;
        if (!SourVisualConfig.wmEnabled) return;

        String nick = client.getSession().getUsername();
        int fps = client.getCurrentFps();
        // Если у тебя другой mappings-геттер FPS, замени client.getCurrentFps()
        String time = LocalTime.now().format(TIME_FMT);

        String line = nick + "  |  " + fps + " fps  |  " + time;

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
}
