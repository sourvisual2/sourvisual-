package com.sourvisual.gui;

import net.minecraft.client.gui.DrawContext;

public class RenderUtils {

    public static void fillRounded(DrawContext ctx, int x, int y, int w, int h, int radius, int color) {
        ctx.fill(x + radius, y, x + w - radius, y + h, color);
        ctx.fill(x, y + radius, x + w, y + h - radius, color);

        fillCorner(ctx, x, y, radius, color, true, true);
        fillCorner(ctx, x + w - radius, y, radius, color, false, true);
        fillCorner(ctx, x, y + h - radius, radius, color, true, false);
        fillCorner(ctx, x + w - radius, y + h - radius, radius, color, false, false);
    }

    // Прямоугольник со скруглёнными только верхними углами (низ прямой)
    public static void fillRoundedTop(DrawContext ctx, int x, int y, int w, int h, int radius, int color) {
        ctx.fill(x, y + radius, x + w, y + h, color);
        ctx.fill(x + radius, y, x + w - radius, y + radius, color);

        fillCorner(ctx, x, y, radius, color, true, true);
        fillCorner(ctx, x + w - radius, y, radius, color, false, true);
    }

    // Прямоугольник со скруглённым только нижним левым углом (остальные прямые)
    public static void fillRoundedBottomLeft(DrawContext ctx, int x, int y, int w, int h, int radius, int color) {
        ctx.fill(x, y, x + w, y + h - radius, color);
        ctx.fill(x + radius, y + h - radius, x + w, y + h, color);

        fillCorner(ctx, x, y + h - radius, radius, color, true, false);
    }

    private static void fillCorner(DrawContext ctx, int x, int y, int radius, int color, boolean left, boolean top) {
        for (int i = 0; i < radius; i++) {
            int dx = radius - i;
            int width = (int) Math.round(radius - Math.sqrt((double) (radius * radius) - (double) (dx * dx)));
            int rowY = top ? y + i : y + radius - 1 - i;
            if (left) {
                ctx.fill(x + width, rowY, x + radius, rowY + 1, color);
            } else {
                ctx.fill(x, rowY, x + radius - width, rowY + 1, color);
            }
        }
    }
                   }
