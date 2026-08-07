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

    // состояние color picker
    private boolean colorPickerOpen = false;

    // слайдеры RGB — dragging
    private int draggingSlider = -1; // 0=R 1=G 2=B

    public VisualMenuScreen() {
        super(Text.literal("Sour Visual"));
    }

    @Override
    protected void init() {
        wX = (this.width  - WIN_W) / 2;
        wY = (this.height - WIN_H) / 2;

        searchField = new TextFieldWidget(
                this.textRenderer,
                wX + SIDE_W + 8,
                wY + 8,
                WIN_W - SIDE_W - 28,
                11,
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

        // заголовок
        int p1w = this.textRenderer.getWidth("Sour ");
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("Sour "), wX + 8, wY + 9, C_ACCENT1);
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("Visual"), wX + 8 + p1w, wY + 9, C_ACCENT2);

        ctx.drawHorizontalLine(wX, wX + WIN_W, wY + HEADER_H, C_DIVIDER);
        ctx.drawVerticalLine(wX + SIDE_W, wY, wY + HEADER_H, C_BORDER);

        // поиск
        ctx.fill(wX + SIDE_W + 5, wY + 6, wX + WIN_W - 5, wY + 19, 0x22FFFFFF);
        searchField.render(ctx, mx, my, delta);
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("⌕"), wX + WIN_W - 12, wY + 9, C_DIM);

        // категории
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

        // контент
        renderContent(ctx, wX + SIDE_W + 8, wY + HEADER_H + 8, mx, my);

        // color picker поверх всего
        if (colorPickerOpen) {
            renderColorPicker(ctx, mx, my);
        }
    }

    // ── Контент панелей ──────────────────────────────────────────────────────
    private void renderContent(DrawContext ctx, int x, int y, int mx, int my) {
        switch (selected) {
            case VISUAL -> renderVisual(ctx, x, y, mx, my);
            case EFFECTS -> ctx.drawTextWithShadow(
                    this.textRenderer, Text.literal("— empty —"), x, y, C_DIM);
            case SETTINGS -> ctx.drawTextWithShadow(
                    this.textRenderer, Text.literal("— empty —"), x, y, C_DIM);
        }
    }

    private void renderVisual(DrawContext ctx, int x, int y, int mx, int my) {
        int rowW = WIN_W - SIDE_W - 12;
        int rowH = 34;

        // фон строки
        ctx.fill(x - 2, y, x + rowW, y + rowH, 0x22FFFFFF);

        // название + описание
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("Hit Color"), x + 3, y + 6, C_WHITE);
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("Цвет скина при ударе."), x + 3, y + 18, C_DIM);

        // превью текущего цвета (маленький квадрат)
        int previewColor = 0xFF000000
                | (ModConfig.hitColorR << 16)
                | (ModConfig.hitColorG << 8)
                | ModConfig.hitColorB;
        ctx.fill(x + rowW - 52, y + 9, x + rowW - 38, y + 23, previewColor);
        borderRounded(ctx, x + rowW - 52, y + 9, 14, 14, 2, C_BORDER);

        // шестерёнка ⚙
        boolean gearHov = mx >= x + rowW - 36 && mx <= x + rowW - 24
                       && my >= y + 9          && my <= y + 23;
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("⚙"),
                x + rowW - 36, y + 11,
                gearHov ? C_WHITE : C_DIM);

        // тумблер
        boolean on = ModConfig.hitColorEnabled;
        int tx = x + rowW - 20;
        int ty = y + 11;
        ctx.fill(tx, ty, tx + 18, ty + 10, on ? 0xFF5A3FBF : 0xFF333340);
        borderRounded(ctx, tx, ty, 18, 10, 3, C_BORDER);
        int cx = on ? tx + 10 : tx + 2;
        ctx.fill(cx, ty + 2, cx + 6, ty + 6, on ? C_ACCENT2 : C_DIM);
    }

    // ── Color Picker ─────────────────────────────────────────────────────────
    private void renderColorPicker(DrawContext ctx, int mx, int my) {
        int pw = 160;
        int ph = 120;
        int px = wX + (WIN_W - pw) / 2;
        int py = wY + (WIN_H - ph) / 2;

        // фон пикера
        fillRounded(ctx, px, py, pw, ph, R, 0xF01A1A24);
        borderRounded(ctx, px, py, pw, ph, R, C_ACCENT1);

        // заголовок
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("Hit Color"), px + 8, py + 7, C_WHITE);

        // кнопка закрыть
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("✕"), px + pw - 14, py + 7, C_DIM);

        int sy = py + 22; // старт слайдеров

        // слайдеры R G B
        drawSlider(ctx, px + 8, sy,      pw - 16, "R",
                ModConfig.hitColorR, 0xFFFF4444, mx, my, 0);
        drawSlider(ctx, px + 8, sy + 28, pw - 16, "G",
                ModConfig.hitColorG, 0xFF44FF44, mx, my, 1);
        drawSlider(ctx, px + 8, sy + 56, pw - 16, "B",
                ModConfig.hitColorB, 0xFF4444FF, mx, my, 2);

        // превью итогового цвета
        int previewColor = 0xFF000000
                | (ModConfig.hitColorR << 16)
                | (ModConfig.hitColorG << 8)
                | ModConfig.hitColorB;
        int prevX = px + 8;
        int prevY = sy + 88;
        ctx.fill(prevX, prevY, prevX + pw - 16, prevY + 16, previewColor);
        borderRounded(ctx, prevX, prevY, pw - 16, 16, 2, C_BORDER);

        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal("Preview"),
                prevX + 4, prevY + 4, 0xAAFFFFFF);
    }

    private void drawSlider(DrawContext ctx, int x, int y, int w,
                            String label, int value, int trackColor,
                            int mx, int my, int index) {
        int trackW = w - 20;
        int trackX = x + 16;
        int trackY = y + 6;
        int trackH = 6;

        // метка
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal(label), x, y + 4, trackColor);

        // трек фон
        ctx.fill(trackX, trackY, trackX + trackW, trackY + trackH, 0xFF333340);
        borderRounded(ctx, trackX, trackY, trackW, trackH, 2, C_BORDER);

        // заполненная часть
        int filled = (int)((value / 255.0f) * trackW);
        ctx.fill(trackX, trackY, trackX + filled, trackY + trackH, trackColor);

        // ползунок
        int knobX = trackX + filled - 3;
        ctx.fill(knobX, trackY - 2, knobX + 6, trackY + trackH + 2, C_WHITE);
        borderRounded(ctx, knobX, trackY - 2, 6, trackH + 4, 2, C_BORDER);

        // значение
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal(String.valueOf(value)),
                trackX + trackW + 4, y + 4, C_DIM);
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int button) {

        // color picker открыт — обрабатываем его клики
        if (colorPickerOpen) {
            int pw = 160, ph = 120;
            int px = wX + (WIN_W - pw) / 2;
            int py = wY + (WIN_H - ph) / 2;

            // кнопка ✕
            if (mx >= px + pw - 18 && mx <= px + pw - 4
             && my >= py + 4       && my <= py + 18) {
                colorPickerOpen = false;
                ModConfig.save();
                return true;
            }

            // клик по слайдерам
            int sy = py + 22;
            int[] offsets = {0, 28, 56};
            for (int i = 0; i < 3; i++) {
                int trackX = px + 24;
                int trackW = pw - 40;
                int trackY = sy + offsets[i] + 3;
                if (mx >= trackX && mx <= trackX + trackW
                 && my >= trackY - 4 && my <= trackY + 10) {
                    draggingSlider = i;
                    updateSlider(i, (int) mx, trackX, trackW);
                    return true;
                }
            }
            return true; // поглощаем все клики внутри пикера
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

        // клики в Visual панели
        if (selected == MenuCategory.VISUAL) {
            int x    = wX + SIDE_W + 8;
            int y    = wY + HEADER_H + 8;
            int rowW = WIN_W - SIDE_W - 12;

            // шестерёнка
            if (mx >= x + rowW - 36 && mx <= x + rowW - 24
             && my >= y + 9         && my <= y + 23) {
                colorPickerOpen = true;
                return true;
            }

            // тумблер
            int tx = x + rowW - 20;
            int ty = y + 11;
            if (mx >= tx && mx <= tx + 18 && my >= ty && my <= ty + 10) {
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
            int trackX = px + 24;
            int trackW = pw - 40;
            updateSlider(draggingSlider, (int) mx, trackX, trackW);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (draggingSlider >= 0) {
            draggingSlider = -1;
            ModConfig.save();
        }
        return super.mouseReleased(mx, my, button);
    }

    private void updateSlider(int index, int mouseX, int trackX, int trackW) {
        int val = (int)(((mouseX - trackX) / (float) trackW) * 255);
        val = Math.max(0, Math.min(255, val));
        switch (index) {
            case 0 -> ModConfig.hitColorR = val;
            case 1 -> ModConfig.hitColorG = val;
            case 2 -> ModConfig.hitColorB = val;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            if (colorPickerOpen) {
                colorPickerOpen = false;
                ModConfig.save();
            } else {
                this.close();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }

    // ── Хелперы отрисовки ────────────────────────────────────────────────────
    private void fillRounded(DrawContext ctx, int x, int y, int w, int h,
                             int r, int color) {
        ctx.fill(x + r, y,     x + w - r, y + h,     color);
        ctx.fill(x,     y + r, x + r,     y + h - r, color);
        ctx.fill(x+w-r, y + r, x + w,     y + h - r, color);
        fillCorner(ctx, x,       y,       r,  1,  1, color);
        fillCorner(ctx, x+w-r,   y,       r, -1,  1, color);
        fillCorner(ctx, x,       y+h-r,   r,  1, -1, color);
        fillCorner(ctx, x+w-r,   y+h-r,   r, -1, -1, color);
    }

    private void fillCorner(DrawContext ctx, int cx, int cy, int r,
                            int dx, int dy, int color) {
        for (int i = 0; i < r; i++)
            for (int j = 0; j < r; j++) {
                if (Math.sqrt((r-1-i)*(r-1-i)+(r-1-j)*(r-1-j)) < r - 0.5) {
                    int px = cx + (dx > 0 ? i : r-1-i);
                    int py = cy + (dy > 0 ? j : r-1-j);
                    ctx.fill(px, py, px+1, py+1, color);
                }
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
