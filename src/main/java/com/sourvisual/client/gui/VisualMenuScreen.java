package com.sourvisual.client.gui;

import com.sourvisual.client.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class VisualMenuScreen extends Screen {

    private static final int WIN_W    = 315;
    private static final int WIN_H    = 195;
    private static final int SIDE_W   = 82;
    private static final int HEADER_H = 27;
    private static final int R        = 4;

    private static final int C_BG      = 0xEE0D0D12;
    private static final int C_BORDER  = 0xFF252530;
    private static final int C_DIVIDER = 0xFF1E1E28;
    private static final int C_SEL_BG  = 0x33FFFFFF;
    private static final int C_WHITE   = 0xFFFFFFFF;
    private static final int C_DIM     = 0xFF888899;
    private static final int C_ACCENT1 = 0xFF9B6FFF;
    private static final int C_ACCENT2 = 0xFFCC99FF;
    private static final int C_HOVER   = 0xFFCCCCDD;

    private int wX, wY;
    private TextFieldWidget searchField;
    private MenuCategory selected = MenuCategory.VISUAL;

    private boolean colorPickerOpen = false;
    private int draggingSlider = -1;

    public VisualMenuScreen() {
        super(Text.literal("Sour Visual"));
    }

    @Override
    protected void init() {
        wX = (this.width  - WIN_W) / 2;
        wY = (this.height - WIN_H) / 2;

        searchField = new TextFieldWidget(
                this.textRenderer,
                wX + SIDE_W + 8, wY + 8,
                WIN_W - SIDE_W - 28, 11,
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

        fillRounded(ctx, wX, wY, WIN_W, WIN_H, R, C_BG);
        borderRounded(ctx, wX, wY, WIN_W, WIN_H, R, C_BORDER);
        ctx.drawVerticalLine(wX + SIDE_W, wY, wY + WIN_H, C_BORDER);

        int p1w = this.textRenderer.getWidth("Sour ");
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("Sour "), wX + 8, wY + 9, C_ACCENT1);
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("Visual"), wX + 8 + p1w, wY + 9, C_ACCENT2);

        ctx.drawHorizontalLine(wX, wX + WIN_W, wY + HEADER_H, C_DIVIDER);
        ctx.drawVerticalLine(wX + SIDE_W, wY, wY + HEADER_H, C_BORDER);

        ctx.fill(wX + SIDE_W + 5, wY + 6, wX + WIN_W - 5, wY + 19, 0x22FFFFFF);
        searchField.render(ctx, mx, my, delta);
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("⌕"), wX + WIN_W - 12, wY + 9, C_DIM);

        int catY = wY + HEADER_H + 7;
        for (MenuCategory cat : MenuCategory.values()) {
            boolean hov = mx >= wX + 3 && mx <= wX + SIDE_W - 3
                       && my >= catY   && my <= catY + 18;
            boolean sel = cat == selected;
            if (sel) ctx.fill(wX + 3, catY, wX + SIDE_W - 3, catY + 18, C_SEL_BG);
            int col = sel ? C_WHITE : (hov ? C_HOVER : C_DIM);
            ctx.drawTextWithShadow(this.textRenderer,
                    Text.literal(iconFor(cat) + " " + cat.label),
                    wX + 7, catY + 5, col);
            catY += 22;
        }

        renderContent(ctx, wX + SIDE_W + 8, wY + HEADER_H + 8, mx, my);

        if (colorPickerOpen) renderColorPicker(ctx, mx, my);
    }

    private void renderContent(DrawContext ctx, int x, int y, int mx, int my) {
        switch (selected) {
            case VISUAL   -> renderVisual(ctx, x, y, mx, my);
            case EFFECTS  -> ctx.drawTextWithShadow(this.textRenderer,
                    Text.literal("— empty —"), x, y, C_DIM);
            case SETTINGS -> ctx.drawTextWithShadow(this.textRenderer,
                    Text.literal("— empty —"), x, y, C_DIM);
        }
    }

    private void renderVisual(DrawContext ctx, int x, int y, int mx, int my) {
        int rowW = WIN_W - SIDE_W - 12;
        int rowH = 34;
        ctx.fill(x - 2, y, x + rowW, y + rowH, 0x22FFFFFF);

        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("Hit Color"), x + 3, y + 6, C_WHITE);
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("Цвет скина при ударе."), x + 3, y + 18, C_DIM);

        // ── Круглый превью цвета ────────────────────────────────────────────
        int circX = x + rowW - 52;
        int circY = y + 9;
        int circD = 14; // диаметр
        drawCircle(ctx, circX, circY, circD,
                0xFF000000 | (ModConfig.hitColorR << 16)
                | (ModConfig.hitColorG << 8) | ModConfig.hitColorB);

        // ── Шестерёнка ──────────────────────────────────────────────────────
        boolean gearHov = mx >= circX + circD + 2 && mx <= circX + circD + 14
                       && my >= circY              && my <= circY + circD;
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("⚙"),
                circX + circD + 2, circY + 2,
                gearHov ? C_WHITE : C_DIM);

        // ── Овальный переключатель ──────────────────────────────────────────
        boolean on = ModConfig.hitColorEnabled;
        int sw = 28; // ширина
        int sh = 14; // высота
        int sx = x + rowW - sw - 2;
        int sy = y + rowH / 2 - sh / 2;

        // фон переключателя
        int bgCol = on ? 0xFF4A3A9F : 0xFF2A2A35;
        fillOval(ctx, sx, sy, sw, sh, bgCol);
        borderOval(ctx, sx, sy, sw, sh, C_BORDER);

        // кружок внутри
        int knobD = sh - 4;
        int knobX = on ? sx + sw - knobD - 2 : sx + 2;
        int knobY = sy + 2;
        drawCircle(ctx, knobX, knobY, knobD, on ? C_WHITE : 0xFF555566);
    }

    // ── Color Picker ─────────────────────────────────────────────────────────
    private void renderColorPicker(DrawContext ctx, int mx, int my) {
        int pw = 160, ph = 120;
        int px = wX + (WIN_W - pw) / 2;
        int py = wY + (WIN_H - ph) / 2;

        fillRounded(ctx, px, py, pw, ph, R, 0xF01A1A24);
        borderRounded(ctx, px, py, pw, ph, R, C_ACCENT1);

        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("Выбери цвет"), px + 8, py + 7, C_WHITE);
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("✕"), px + pw - 14, py + 7, C_DIM);

        int sy = py + 22;
        drawSlider(ctx, px + 8, sy,      pw - 16, "R", ModConfig.hitColorR, 0xFFFF4444);
        drawSlider(ctx, px + 8, sy + 28, pw - 16, "G", ModConfig.hitColorG, 0xFF44FF44);
        drawSlider(ctx, px + 8, sy + 56, pw - 16, "B", ModConfig.hitColorB, 0xFF6666FF);

        // превью — круг
        int previewColor = 0xFF000000
                | (ModConfig.hitColorR << 16)
                | (ModConfig.hitColorG << 8)
                | ModConfig.hitColorB;
        int cD = 18;
        int cX = px + (pw - cD) / 2;
        int cY = sy + 86;
        drawCircle(ctx, cX, cY, cD, previewColor);
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("Preview"), cX + cD + 4, cY + 4, C_DIM);
    }

    private void drawSlider(DrawContext ctx, int x, int y, int w,
                            String label, int value, int color) {
        int tW = w - 24;
        int tX = x + 16;
        int tY = y + 6;
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal(label), x, y + 3, color);
        ctx.fill(tX, tY, tX + tW, tY + 6, 0xFF333340);
        int filled = (int)((value / 255f) * tW);
        ctx.fill(tX, tY, tX + filled, tY + 6, color);
        int kX = tX + filled - 3;
        ctx.fill(kX, tY - 2, kX + 6, tY + 8, C_WHITE);
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal(String.valueOf(value)),
                tX + tW + 4, y + 3, C_DIM);
    }

    // ── Рисуем круг пикселями ────────────────────────────────────────────────
    private void drawCircle(DrawContext ctx, int x, int y, int d, int color) {
        float r = d / 2f;
        float cx = x + r;
        float cy = y + r;
        for (int i = x; i < x + d; i++) {
            for (int j = y; j < y + d; j++) {
                double dist = Math.sqrt((i - cx + 0.5) * (i - cx + 0.5)
                                      + (j - cy + 0.5) * (j - cy + 0.5));
                if (dist <= r) {
                    ctx.fill(i, j, i + 1, j + 1, color);
                }
            }
        }
    }

    // ── Овал (закруглённый прямоугольник с r = h/2) ─────────────────────────
    private void fillOval(DrawContext ctx, int x, int y, int w, int h, int color) {
        fillRounded(ctx, x, y, w, h, h / 2, color);
    }

    private void borderOval(DrawContext ctx, int x, int y, int w, int h, int color) {
        borderRounded(ctx, x, y, w, h, h / 2, color);
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (colorPickerOpen) {
            int pw = 160, ph = 120;
            int px = wX + (WIN_W - pw) / 2;
            int py = wY + (WIN_H - ph) / 2;

            // ✕
            if (mx >= px + pw - 18 && mx <= px + pw - 4
             && my >= py + 4       && my <= py + 18) {
                colorPickerOpen = false;
                ModConfig.save();
                return true;
            }

            int sy = py + 22;
            int[] offY = {0, 28, 56};
            for (int i = 0; i < 3; i++) {
                int tX = px + 24, tW = pw - 40;
                int tY = sy + offY[i] + 3;
                if (mx >= tX && mx <= tX + tW
                 && my >= tY - 4 && my <= tY + 10) {
                    draggingSlider = i;
                    applySlider(i, (int) mx, tX, tW);
                    return true;
                }
            }
            return true;
        }

        // категории
        int catY = wY + HEADER_H + 7;
        for (MenuCategory cat : MenuCategory.values()) {
            if (mx >= wX + 3 && mx <= wX + SIDE_W - 3
             && my >= catY   && my <= catY + 18) {
                selected = cat;
                return true;
            }
            catY += 22;
        }

        if (selected == MenuCategory.VISUAL) {
            int x    = wX + SIDE_W + 8;
            int y    = wY + HEADER_H + 8;
            int rowW = WIN_W - SIDE_W - 12;
            int rowH = 34;

            // шестерёнка
            int circX = x + rowW - 52;
            int circY = y + 9;
            int circD = 14;
            if (mx >= circX + circD + 2 && mx <= circX + circD + 14
             && my >= circY             && my <= circY + circD) {
                colorPickerOpen = true;
                return true;
            }

            // овальный переключатель
            int sw = 28, sh = 14;
            int sx = x + rowW - sw - 2;
            int sy = y + rowH / 2 - sh / 2;
            if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + sh) {
                ModConfig.hitColorEnabled = !ModConfig.hitColorEnabled;
                ModConfig.save();
                return true;
            }
        }

        if (searchField.isMouseOver(mx, my)) searchField.setFocused(true);
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button,
                                double dx, double dy) {
        if (draggingSlider >= 0 && colorPickerOpen) {
            int pw = 160;
            int px = wX + (WIN_W - pw) / 2;
            applySlider(draggingSlider, (int) mx, px + 24, pw - 40);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (draggingSlider >= 0) { draggingSlider = -1; ModConfig.save(); }
        return super.mouseReleased(mx, my, button);
    }

    private void applySlider(int i, int mouseX, int tX, int tW) {
        int v = Math.max(0, Math.min(255,
                (int)(((mouseX - tX) / (float) tW) * 255)));
        switch (i) {
            case 0 -> ModConfig.hitColorR = v;
            case 1 -> ModConfig.hitColorG = v;
            case 2 -> ModConfig.hitColorB = v;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            if (colorPickerOpen) { colorPickerOpen = false; ModConfig.save(); }
            else this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }

    private void fillRounded(DrawContext ctx, int x, int y, int w, int h,
                             int r, int color) {
        ctx.fill(x+r, y,   x+w-r, y+h,   color);
        ctx.fill(x,   y+r, x+r,   y+h-r, color);
        ctx.fill(x+w-r, y+r, x+w, y+h-r, color);
        fillCorner(ctx, x,     y,     r,  1,  1, color);
        fillCorner(ctx, x+w-r, y,     r, -1,  1, color);
        fillCorner(ctx, x,     y+h-r, r,  1, -1, color);
        fillCorner(ctx, x+w-r, y+h-r, r, -1, -1, color);
    }

    private void fillCorner(DrawContext ctx, int cx, int cy, int r,
                            int dx, int dy, int color) {
        for (int i = 0; i < r; i++)
            for (int j = 0; j < r; j++)
                if (Math.sqrt((r-1-i)*(r-1-i)+(r-1-j)*(r-1-j)) < r - 0.5) {
                    int px = cx + (dx > 0 ? i : r-1-i);
                    int py = cy + (dy > 0 ? j : r-1-j);
                    ctx.fill(px, py, px+1, py+1, color);
                }
    }

    private void borderRounded(DrawContext ctx, int x, int y, int w, int h,
                               int r, int color) {
        ctx.drawHorizontalLine(x+r, x+w-r, y,   color);
        ctx.drawHorizontalLine(x+r, x+w-r, y+h, color);
        ctx.drawVerticalLine(x,   y+r, y+h-r, color);
        ctx.drawVerticalLine(x+w, y+r, y+h-r, color);
        drawArc(ctx, x+r,   y+r,   r, color, 180, 270);
        drawArc(ctx, x+w-r, y+r,   r, color, 270, 360);
        drawArc(ctx, x+r,   y+h-r, r, color,  90, 180);
        drawArc(ctx, x+w-r, y+h-r, r, color,   0,  90);
    }

    private void drawArc(DrawContext ctx, int cx, int cy, int r,
                         int color, int s, int e) {
        for (int d = s; d <= e; d += 2) {
            double rad = Math.toRadians(d);
            int px = cx + (int) Math.round((r-1)*Math.cos(rad));
            int py = cy - (int) Math.round((r-1)*Math.sin(rad));
            ctx.fill(px, py, px+1, py+1, color);
        }
    }

    private String iconFor(MenuCategory cat) {
        return switch (cat) {
            case EFFECTS  -> "★";
            case VISUAL   -> "◉";
            case SETTINGS -> "⚙";
        };
    }
}
