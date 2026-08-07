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

    // HSV picker state
    private float hue        = 0.75f; // 0..1
    private float saturation = 0.5f;  // 0..1
    private float brightness = 0.8f;  // 0..1

    private boolean draggingSV  = false;
    private boolean draggingHue = false;

    public VisualMenuScreen() {
        super(Text.literal("Sour Visual"));
        syncHsvFromConfig();
    }

    // синхронизируем HSV из RGB конфига
    private void syncHsvFromConfig() {
        float[] hsv = rgbToHsv(ModConfig.hitColorR, ModConfig.hitColorG, ModConfig.hitColorB);
        hue        = hsv[0];
        saturation = hsv[1];
        brightness = hsv[2];
    }

    private void applyHsvToConfig() {
        int[] rgb = hsvToRgb(hue, saturation, brightness);
        ModConfig.hitColorR = rgb[0];
        ModConfig.hitColorG = rgb[1];
        ModConfig.hitColorB = rgb[2];
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
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Sour "),  wX + 8, wY + 9, C_ACCENT1);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Visual"), wX + 8 + p1w, wY + 9, C_ACCENT2);

        ctx.drawHorizontalLine(wX, wX + WIN_W, wY + HEADER_H, C_DIVIDER);
        ctx.drawVerticalLine(wX + SIDE_W, wY, wY + HEADER_H, C_BORDER);

        ctx.fill(wX + SIDE_W + 5, wY + 6, wX + WIN_W - 5, wY + 19, 0x22FFFFFF);
        searchField.render(ctx, mx, my, delta);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("⌕"), wX + WIN_W - 12, wY + 9, C_DIM);

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
            case EFFECTS  -> ctx.drawTextWithShadow(this.textRenderer, Text.literal("— empty —"), x, y, C_DIM);
            case SETTINGS -> ctx.drawTextWithShadow(this.textRenderer, Text.literal("— empty —"), x, y, C_DIM);
        }
    }

    // ── Visual панель ────────────────────────────────────────────────────────
    private void renderVisual(DrawContext ctx, int x, int y, int mx, int my) {
        int rowW = WIN_W - SIDE_W - 12;
        int rowH = 34;
        ctx.fill(x - 2, y, x + rowW, y + rowH, 0x22FFFFFF);

        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Hit Color"), x + 3, y + 6, C_WHITE);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Цвет скина при ударе."), x + 3, y + 18, C_DIM);

        // круглый превью цвета
        int circD = 14;
        int circX = x + rowW - 54;
        int circY = y + rowH / 2 - circD / 2;
        int previewColor = 0xFF000000
                | (ModConfig.hitColorR << 16)
                | (ModConfig.hitColorG << 8)
                | ModConfig.hitColorB;
        drawCircle(ctx, circX, circY, circD, previewColor);

        // шестерёнка
        boolean gearHov = mx >= circX + circD + 2 && mx <= circX + circD + 12
                       && my >= circY             && my <= circY + circD;
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("⚙"),
                circX + circD + 2, circY + 2, gearHov ? C_WHITE : C_DIM);

        // овальный переключатель — фиксированные размеры внутри строки
        boolean on = ModConfig.hitColorEnabled;
        int sw = 26;
        int sh = 12;
        int sx = x + rowW - sw - 2;
        int sy = y + rowH / 2 - sh / 2; // центрируем по вертикали строки

        // фон овала
        fillOval(ctx, sx, sy, sw, sh, on ? 0xFF4A3A9F : 0xFF2A2A35);
        borderOval(ctx, sx, sy, sw, sh, C_BORDER);

        // кружок переключателя — строго внутри
        int knobD = sh - 4;
        int knobX = on ? sx + sw - knobD - 2 : sx + 2;
        int knobY = sy + 2;
        drawCircle(ctx, knobX, knobY, knobD, on ? C_WHITE : 0xFF555566);
    }

    // ── HSV Color Picker ─────────────────────────────────────────────────────
    private static final int PW = 170;
    private static final int PH = 155;
    private static final int SV_SIZE = 100; // квадрат SV
    private static final int HUE_W   = 14;  // полоса оттенка
    private static final int GAP     = 6;

    private int pickerX() { return wX + (WIN_W - PW) / 2; }
    private int pickerY() { return wY + (WIN_H - PH) / 2; }
    private int svX()     { return pickerX() + 8; }
    private int svY()     { return pickerY() + 26; }
    private int hueX()    { return svX() + SV_SIZE + GAP; }

    private void renderColorPicker(DrawContext ctx, int mx, int my) {
        int px = pickerX(), py = pickerY();

        fillRounded(ctx, px, py, PW, PH, R, 0xF01A1A24);
        borderRounded(ctx, px, py, PW, PH, R, C_ACCENT1);

        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Выбери цвет"), px + 8, py + 8, C_WHITE);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("✕"), px + PW - 14, py + 8, C_DIM);

        int sx = svX(), sy = svY();

        // ── SV квадрат ───────────────────────────────────────────────────────
        for (int i = 0; i < SV_SIZE; i++) {
            for (int j = 0; j < SV_SIZE; j++) {
                float s = i / (float)(SV_SIZE - 1);
                float v = 1f - j / (float)(SV_SIZE - 1);
                int[] rgb = hsvToRgb(hue, s, v);
                ctx.fill(sx + i, sy + j, sx + i + 1, sy + j + 1,
                        0xFF000000 | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2]);
            }
        }
        borderRounded(ctx, sx, sy, SV_SIZE, SV_SIZE, 2, C_BORDER);

        // курсор на SV
        int cursorX = sx + (int)(saturation * (SV_SIZE - 1));
        int cursorY = sy + (int)((1f - brightness) * (SV_SIZE - 1));
        ctx.fill(cursorX - 3, cursorY - 1, cursorX + 4, cursorY + 2, C_WHITE);
        ctx.fill(cursorX - 1, cursorY - 3, cursorX + 2, cursorY + 4, C_WHITE);
        ctx.fill(cursorX - 2, cursorY - 1, cursorX + 3, cursorY + 2, 0xFF000000);

        // ── Hue полоса ───────────────────────────────────────────────────────
        int hx = hueX(), hy = sy;
        for (int j = 0; j < SV_SIZE; j++) {
            float h = j / (float)(SV_SIZE - 1);
            int[] rgb = hsvToRgb(h, 1f, 1f);
            ctx.fill(hx, hy + j, hx + HUE_W, hy + j + 1,
                    0xFF000000 | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2]);
        }
        borderRounded(ctx, hx, hy, HUE_W, SV_SIZE, 2, C_BORDER);

        // курсор на Hue
        int hueCurY = hy + (int)(hue * (SV_SIZE - 1));
        ctx.fill(hx - 2, hueCurY - 1, hx + HUE_W + 2, hueCurY + 2, C_WHITE);

        // ── Превью ───────────────────────────────────────────────────────────
        int previewColor = 0xFF000000
                | (ModConfig.hitColorR << 16)
                | (ModConfig.hitColorG << 8)
                | ModConfig.hitColorB;
        int prevD = 16;
        int prevX = hx + HUE_W + GAP;
        int prevY = hy + SV_SIZE / 2 - prevD / 2;
        drawCircle(ctx, prevX, prevY, prevD, previewColor);
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int button) {

        if (colorPickerOpen) {
            int px = pickerX(), py = pickerY();
            int sx = svX(), sy = svY();
            int hx = hueX();

            // закрыть ✕
            if (mx >= px + PW - 18 && mx <= px + PW - 4
             && my >= py + 4       && my <= py + 18) {
                colorPickerOpen = false;
                ModConfig.save();
                return true;
            }

            // SV квадрат
            if (mx >= sx && mx <= sx + SV_SIZE
             && my >= sy && my <= sy + SV_SIZE) {
                draggingSV = true;
                updateSV((int) mx, (int) my);
                return true;
            }

            // Hue полоса
            if (mx >= hx && mx <= hx + HUE_W
             && my >= sy && my <= sy + SV_SIZE) {
                draggingHue = true;
                updateHue((int) my);
                return true;
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
            int circD = 14;
            int circX = x + rowW - 54;
            int circY = y + rowH / 2 - circD / 2;

            // шестерёнка
            if (mx >= circX + circD + 2 && mx <= circX + circD + 12
             && my >= circY             && my <= circY + circD) {
                colorPickerOpen = true;
                syncHsvFromConfig();
                return true;
            }

            // переключатель
            int sw = 26, sh = 12;
            int switchX = x + rowW - sw - 2;
            int switchY = y + rowH / 2 - sh / 2;
            if (mx >= switchX && mx <= switchX + sw
             && my >= switchY && my <= switchY + sh) {
                ModConfig.hitColorEnabled = !ModConfig.hitColorEnabled;
                ModConfig.save();
                return true;
            }
        }

        if (searchField.isMouseOver(mx, my)) searchField.setFocused(true);
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (colorPickerOpen) {
            if (draggingSV)  { updateSV((int) mx, (int) my);  return true; }
            if (draggingHue) { updateHue((int) my);            return true; }
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (draggingSV || draggingHue) {
            draggingSV = false;
            draggingHue = false;
            ModConfig.save();
        }
        return super.mouseReleased(mx, my, button);
    }

    private void updateSV(int mx, int my) {
        int sx = svX(), sy = svY();
        saturation = Math.max(0, Math.min(1, (mx - sx) / (float)(SV_SIZE - 1)));
        brightness = Math.max(0, Math.min(1, 1f - (my - sy) / (float)(SV_SIZE - 1)));
        applyHsvToConfig();
    }

    private void updateHue(int my) {
        int sy = svY();
        hue = Math.max(0, Math.min(1, (my - sy) / (float)(SV_SIZE - 1)));
        applyHsvToConfig();
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

    // ── Рисование ────────────────────────────────────────────────────────────
    private void drawCircle(DrawContext ctx, int x, int y, int d, int color) {
        float r = d / 2f;
        float cx = x + r - 0.5f;
        float cy = y + r - 0.5f;
        for (int i = x; i < x + d; i++)
            for (int j = y; j < y + d; j++)
                if (Math.sqrt((i-cx)*(i-cx)+(j-cy)*(j-cy)) <= r)
                    ctx.fill(i, j, i+1, j+1, color);
    }

    private void fillOval(DrawContext ctx, int x, int y, int w, int h, int color) {
        fillRounded(ctx, x, y, w, h, h / 2, color);
    }

    private void borderOval(DrawContext ctx, int x, int y, int w, int h, int color) {
        borderRounded(ctx, x, y, w, h, h / 2, color);
    }

    private void fillRounded(DrawContext ctx, int x, int y, int w, int h,
                             int r, int color) {
        int rr = Math.min(r, Math.min(w / 2, h / 2));
        ctx.fill(x+rr, y,    x+w-rr, y+h,    color);
        ctx.fill(x,    y+rr, x+rr,   y+h-rr, color);
        ctx.fill(x+w-rr, y+rr, x+w, y+h-rr,  color);
        fillCorner(ctx, x,      y,      rr,  1,  1, color);
        fillCorner(ctx, x+w-rr, y,      rr, -1,  1, color);
        fillCorner(ctx, x,      y+h-rr, rr,  1, -1, color);
        fillCorner(ctx, x+w-rr, y+h-rr, rr, -1, -1, color);
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
        int rr = Math.min(r, Math.min(w / 2, h / 2));
        ctx.drawHorizontalLine(x+rr, x+w-rr, y,   color);
        ctx.drawHorizontalLine(x+rr, x+w-rr, y+h, color);
        ctx.drawVerticalLine(x,   y+rr, y+h-rr, color);
        ctx.drawVerticalLine(x+w, y+rr, y+h-rr, color);
        drawArc(ctx, x+rr,   y+rr,   rr, color, 180, 270);
        drawArc(ctx, x+w-rr, y+rr,   rr, color, 270, 360);
        drawArc(ctx, x+rr,   y+h-rr, rr, color,  90, 180);
        drawArc(ctx, x+w-rr, y+h-rr, rr, color,   0,  90);
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

    // ── HSV конвертация ───────────────────────────────────────────────────────
    private static int[] hsvToRgb(float h, float s, float v) {
        float r, g, b;
        if (s == 0) { r = g = b = v; }
        else {
            int i = (int)(h * 6);
            float f = h * 6 - i;
            float p = v * (1 - s);
            float q = v * (1 - f * s);
            float t = v * (1 - (1 - f) * s);
            switch (i % 6) {
                case 0 -> { r=v; g=t; b=p; }
                case 1 -> { r=q; g=v; b=p; }
                case 2 -> { r=p; g=v; b=t; }
                case 3 -> { r=p; g=q; b=v; }
                case 4 -> { r=t; g=p; b=v; }
                default-> { r=v; g=p; b=q; }
            }
        }
        return new int[]{(int)(r*255),(int)(g*255),(int)(b*255)};
    }

    private static float[] rgbToHsv(int r, int g, int b) {
        float rf = r/255f, gf = g/255f, bf = b/255f;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;
        float h = 0, s = max == 0 ? 0 : delta / max, v = max;
        if (delta != 0) {
            if      (max == rf) h = ((gf - bf) / delta % 6) / 6f;
            else if (max == gf) h = ((bf - rf) / delta + 2) / 6f;
            else                h = ((rf - gf) / delta + 4) / 6f;
            if (h < 0) h += 1;
        }
        return new float[]{h, s, v};
    }

    private String iconFor(MenuCategory cat) {
        return switch (cat) {
            case EFFECTS  -> "★";
            case VISUAL   -> "◉";
            case SETTINGS -> "⚙";
        };
    }
}
