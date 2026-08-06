package com.sourvisual.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class VisualMenuScreen extends Screen {

    private static final int WIN_W    = 420;
    private static final int WIN_H    = 260;
    private static final int SIDE_W   = 110;
    private static final int HEADER_H = 36;
    private static final int R        = 4; // радиус скругления в пикселях

    private static final int C_BG      = 0xEE0D0D12;
    private static final int C_BORDER  = 0xFF252530;
    private static final int C_DIVIDER = 0xFF1E1E28;
    private static final int C_SEL_BG  = 0x33FFFFFF;
    private static final int C_WHITE   = 0xFFFFFFFF;
    private static final int C_DIM     = 0xFF888899;
    private static final int C_ACCENT1 = 0xFF9B6FFF;
    private static final int C_ACCENT2 = 0xFFCC99FF;
    private static final int C_HOVER   = 0xFFCCCCDD;
    private static final int C_TRANS   = 0x00000000;

    private int wX, wY;
    private TextFieldWidget searchField;
    private MenuCategory selected = MenuCategory.VISUAL;

    public VisualMenuScreen() {
        super(Text.literal("Sour Visual"));
    }

    @Override
    protected void init() {
        wX = (this.width  - WIN_W) / 2;
        wY = (this.height - WIN_H) / 2;

        searchField = new TextFieldWidget(
                this.textRenderer,
                wX + SIDE_W + 12,
                wY + 11,
                WIN_W - SIDE_W - 42,
                14,
                Text.literal("")
        );
        searchField.setPlaceholder(Text.literal("Search..."));
        searchField.setMaxLength(64);
        searchField.setDrawsBackground(false);
        this.addSelectableChild(searchField);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        this.renderBackground(ctx, mx, my, delta);

        // основной фон со скруглением
        fillRounded(ctx, wX, wY, WIN_W, WIN_H, R, C_BG);

        // сайдбар (скругление только слева)
        fillRoundedLeft(ctx, wX, wY, SIDE_W, WIN_H, R, C_BG);

        // граница окна скруглённая
        borderRounded(ctx, wX, wY, WIN_W, WIN_H, R, C_BORDER);

        // вертикальный разделитель сайдбара
        ctx.drawVerticalLine(wX + SIDE_W, wY, wY + WIN_H, C_BORDER);

        // заголовок
        int p1w = this.textRenderer.getWidth("Sour ");
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("Sour "), wX + 10, wY + 13, C_ACCENT1);
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("Visual"), wX + 10 + p1w, wY + 13, C_ACCENT2);

        // разделители шапки
        ctx.drawHorizontalLine(wX, wX + WIN_W, wY + HEADER_H, C_DIVIDER);
        ctx.drawVerticalLine(wX + SIDE_W, wY, wY + HEADER_H, C_BORDER);

        // поиск
        borderRounded(ctx, wX + SIDE_W + 8, wY + 8, WIN_W - SIDE_W - 16, 18, 3, C_DIVIDER);
        searchField.render(ctx, mx, my, delta);
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("⌕"), wX + WIN_W - 16, wY + 11, C_DIM);

        // категории
        int catY = wY + HEADER_H + 10;
        for (MenuCategory cat : MenuCategory.values()) {
            boolean hov = mx >= wX + 4 && mx <= wX + SIDE_W - 4
                       && my >= catY   && my <= catY + 20;
            boolean sel = cat == selected;

            if (sel) fillRounded(ctx, wX + 4, catY, SIDE_W - 8, 20, 3, C_SEL_BG);

            int col = sel ? C_WHITE : (hov ? C_HOVER : C_DIM);
            ctx.drawTextWithShadow(this.textRenderer,
                    Text.literal(iconFor(cat)), wX + 10, catY + 6, col);
            ctx.drawTextWithShadow(this.textRenderer,
                    Text.literal(cat.label), wX + 22, catY + 6, col);

            catY += 28;
        }

        // пустой контент
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("— empty —"),
                wX + SIDE_W + 12,
                wY + HEADER_H + 12,
                C_DIM);
    }

    // ── Скруглённый прямоугольник (заливка) ─────────────────────────────────
    private void fillRounded(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        // центр
        ctx.fill(x + r, y,     x + w - r, y + h,     color);
        // левая полоса
        ctx.fill(x,     y + r, x + r,     y + h - r, color);
        // правая полоса
        ctx.fill(x + w - r, y + r, x + w, y + h - r, color);

        // 4 скруглённых угла (рисуем пиксели)
        fillCorner(ctx, x,         y,         r, 1, 1, color);  // top-left
        fillCorner(ctx, x + w - r, y,         r, -1, 1, color); // top-right
        fillCorner(ctx, x,         y + h - r, r, 1, -1, color); // bot-left
        fillCorner(ctx, x + w - r, y + h - r, r, -1, -1, color);// bot-right
    }

    // Скруглённый только слева (для сайдбара)
    private void fillRoundedLeft(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        ctx.fill(x + r, y,     x + w, y + h,     color);
        ctx.fill(x,     y + r, x + r, y + h - r, color);
        fillCorner(ctx, x, y,         r, 1,  1,  color);
        fillCorner(ctx, x, y + h - r, r, 1, -1,  color);
    }

    // Рисует один скруглённый угол пикселями
    private void fillCorner(DrawContext ctx, int cx, int cy, int r, int dx, int dy, int color) {
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < r; j++) {
                // расстояние от угла квадрата
                double dist = Math.sqrt((r - 1 - i) * (r - 1 - i) + (r - 1 - j) * (r - 1 - j));
                if (dist < r - 0.5) {
                    int px = cx + (dx > 0 ? i : r - 1 - i);
                    int py = cy + (dy > 0 ? j : r - 1 - j);
                    ctx.fill(px, py, px + 1, py + 1, color);
                }
            }
        }
    }

    // ── Скруглённая рамка ────────────────────────────────────────────────────
    private void borderRounded(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        // стороны
        ctx.drawHorizontalLine(x + r, x + w - r, y,     color);
        ctx.drawHorizontalLine(x + r, x + w - r, y + h, color);
        ctx.drawVerticalLine(x,     y + r, y + h - r, color);
        ctx.drawVerticalLine(x + w, y + r, y + h - r, color);

        // угловые дуги
        drawArc(ctx, x + r,     y + r,     r, color, 180, 270);
        drawArc(ctx, x + w - r, y + r,     r, color, 270, 360);
        drawArc(ctx, x + r,     y + h - r, r, color,  90, 180);
        drawArc(ctx, x + w - r, y + h - r, r, color,   0,  90);
    }

    // Рисует дугу по точкам
    private void drawArc(DrawContext ctx, int cx, int cy, int r,
                         int color, int startDeg, int endDeg) {
        for (int deg = startDeg; deg <= endDeg; deg += 2) {
            double rad = Math.toRadians(deg);
            int px = cx + (int) Math.round((r - 1) * Math.cos(rad));
            int py = cy - (int) Math.round((r - 1) * Math.sin(rad));
            ctx.fill(px, py, px + 1, py + 1, color);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int catY = wY + HEADER_H + 10;
        for (MenuCategory cat : MenuCategory.values()) {
            if (mx >= wX + 4 && mx <= wX + SIDE_W - 4
             && my >= catY   && my <= catY + 20) {
                selected = cat;
                return true;
            }
            catY += 28;
        }
        if (searchField.isMouseOver(mx, my)) searchField.setFocused(true);
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { this.close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }

    private String iconFor(MenuCategory cat) {
        return switch (cat) {
            case EFFECTS  -> "★";
            case VISUAL   -> "◉";
            case SETTINGS -> "⚙";
        };
    }
                              }
