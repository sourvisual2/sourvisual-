package com.sourvisual.gui;

import com.sourvisual.config.SourVisualConfig;
import com.sourvisual.hud.Watermark;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class VisualScreen extends Screen {

    private static final int SIDE_W   = 82;
    private static final int HEADER_H = 27;
    private static final int R        = 14;
    private static final int RESIZE_GRIP = 10;

    private Tab currentTab = Tab.VISUAL;

    private int winX, winY;

    private boolean draggingWatermark = false;
    private int dragOffX, dragOffY;

    private boolean resizing = false;
    private int resizeStartMouseX, resizeStartMouseY;
    private int resizeStartW, resizeStartH;

    private boolean hitboxSettingsOpen = false;
    private boolean draggingOpacitySlider = false;

    public VisualScreen() {
        super(Text.literal("Sour Visual"));
    }

    @Override
    protected void init() {
        winX = (this.width - SourVisualConfig.winW) / 2;
        winY = (this.height - SourVisualConfig.winH) / 2;
    }

    @Override
    protected void applyBlur(float delta) {
        // блюр фона отключён
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int winW = SourVisualConfig.winW;
        int winH = SourVisualConfig.winH;

        int bg = SourVisualConfig.getBgColor();
        int header = SourVisualConfig.getHeaderColor();
        int side = SourVisualConfig.getSideColor();
        int accent = SourVisualConfig.getAccentColor();
        int text = SourVisualConfig.getTextColor();
        int textDim = SourVisualConfig.getTextDimColor();

        RenderUtils.fillRounded(ctx, winX, winY, winW, winH, R, bg);

        RenderUtils.fillRoundedTop(ctx, winX, winY, winW, HEADER_H, R, header);
        ctx.drawText(this.textRenderer, Text.literal("sour visual"), winX + 10, winY + 9, text, false);

        int sideX = winX;
        int sideY = winY + HEADER_H;
        int sideH = winH - HEADER_H;
        RenderUtils.fillRoundedBottomLeft(ctx, sideX, sideY, SIDE_W, sideH, R, side);

        Tab[] tabs = Tab.values();
        int rowH = 24;
        for (int i = 0; i < tabs.length; i++) {
            Tab tab = tabs[i];
            int rowY = sideY + 6 + i * rowH;
            boolean active = tab == currentTab;
            boolean hovered = mouseX >= sideX && mouseX <= sideX + SIDE_W
                    && mouseY >= rowY && mouseY <= rowY + rowH - 4;

            if (active) {
                ctx.fill(sideX, rowY, sideX + 3, rowY + rowH - 4, accent);
            }
            int textColor = active ? text : (hovered ? text : textDim);
            ctx.drawText(this.textRenderer, Text.literal(tab.label), sideX + 12, rowY + 8, textColor, false);
        }

        int contentX = winX + SIDE_W;
        int contentY = winY + HEADER_H;
        int contentW = winW - SIDE_W;
        int contentH = winH - HEADER_H;
        renderTabContent(ctx, contentX, contentY, contentW, contentH, mouseX, mouseY);

        drawResizeGrip(ctx, winX + winW, winY + winH, accent);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawResizeGrip(DrawContext ctx, int cornerX, int cornerY, int color) {
        for (int i = 0; i < 3; i++) {
            int off = i * 4;
            ctx.fill(cornerX - 3 - off, cornerY - 2, cornerX - 1 - off, cornerY, color);
        }
    }

    private void renderTabContent(DrawContext ctx, int x, int y, int w, int h, int mouseX, int mouseY) {
        switch (currentTab) {
            case VISUAL -> {
                if (hitboxSettingsOpen) {
                    drawHitboxSettings(ctx, x, y, w);
                } else {
                    drawVisual(ctx, x, y, w);
                }
            }
            case UTILITIES -> drawUtilities(ctx, x, y, w);
            case KEYBINDS -> drawPlaceholder(ctx, x, y, "Keybind list goes here");
            case SETTINGS -> drawThemes(ctx, x, y, w, h);
        }
    }

    private void drawPlaceholder(DrawContext ctx, int x, int y, String text) {
        ctx.drawText(this.textRenderer, Text.literal(text), x + 10, y + 10, SourVisualConfig.getTextDimColor(), false);
    }

    // ---------- VISUAL ----------

    private static final int ROW_H = 20;
    private static final int ROW_GAP = 6;

    private void drawVisual(DrawContext ctx, int x, int y, int w) {
        int pad = 10;
        int cx = x + pad;
        int cy = y + pad;
        int rowW = w - pad * 2;

        int text = SourVisualConfig.getTextColor();
        int textDim = SourVisualConfig.getTextDimColor();
        int chip = SourVisualConfig.getChipColor();
        int accent = SourVisualConfig.getAccentColor();

        drawSwitchRow(ctx, cx, cy, rowW, "HitBox", SourVisualConfig.hitboxEnabled, chip, accent, text);

        int hintY = cy + ROW_H + 8;
        ctx.drawText(this.textRenderer, Text.literal("Right-click HitBox to configure"), cx, hintY, textDim, false);
    }

    // ---------- HITBOX SETTINGS ----------

    private static final int SLIDER_W = 180;
    private static final int SLIDER_H = 6;
    private static final int PALETTE_SIZE = 16;
    private static final int PALETTE_GAP = 6;
    private static final int PALETTE_COLS = 10;

    private void drawHitboxSettings(DrawContext ctx, int x, int y, int w) {
        int pad = 10;
        int cx = x + pad;
        int cy = y + pad;
        int rowW = w - pad * 2;

        int text = SourVisualConfig.getTextColor();
        int textDim = SourVisualConfig.getTextDimColor();
        int chip = SourVisualConfig.getChipColor();
        int chipOn = SourVisualConfig.getChipOnColor();
        int accent = SourVisualConfig.getAccentColor();

        ctx.drawText(this.textRenderer, Text.literal("< HitBox"), cx, cy, text, false);

        int filledY = cy + 16;
        drawSwitchRow(ctx, cx, filledY, rowW, "Filled", SourVisualConfig.hitboxFilled, chip, accent, text);

        int colorLabelY = filledY + ROW_H + 10;
        ctx.drawText(this.textRenderer, Text.literal("Color"), cx, colorLabelY, textDim, false);

        int modeChipY = colorLabelY + 12;
        boolean themeMode = SourVisualConfig.hitboxColorMode == SourVisualConfig.HitboxColorMode.THEME;
        int themeChipW = this.textRenderer.getWidth("Theme") + 12;
        int customChipW = this.textRenderer.getWidth("Custom") + 12;
        ctx.fill(cx, modeChipY, cx + themeChipW, modeChipY + 16, themeMode ? chipOn : chip);
        ctx.drawText(this.textRenderer, Text.literal("Theme"), cx + 6, modeChipY + 4, text, false);
        int customChipX = cx + themeChipW + 6;
        ctx.fill(customChipX, modeChipY, customChipX + customChipW, modeChipY + 16, !themeMode ? chipOn : chip);
        ctx.drawText(this.textRenderer, Text.literal("Custom"), customChipX + 6, modeChipY + 4, text, false);

        int nextY = modeChipY + 24;

        if (!themeMode) {
            int paletteY = nextY;
            for (int i = 0; i < SourVisualConfig.HITBOX_PALETTE.length; i++) {
                int col = i % PALETTE_COLS;
                int row = i / PALETTE_COLS;
                int px = cx + col * (PALETTE_SIZE + PALETTE_GAP);
                int py = paletteY + row * (PALETTE_SIZE + PALETTE_GAP);
                int swatchColor = SourVisualConfig.HITBOX_PALETTE[i];
                ctx.fill(px, py, px + PALETTE_SIZE, py + PALETTE_SIZE, swatchColor);
                if (SourVisualConfig.hitboxCustomColor == swatchColor) {
                    ctx.fill(px - 2, py - 2, px + PALETTE_SIZE + 2, py - 1, text);
                    ctx.fill(px - 2, py + PALETTE_SIZE + 1, px + PALETTE_SIZE + 2, py + PALETTE_SIZE + 2, text);
                    ctx.fill(px - 2, py - 2, px - 1, py + PALETTE_SIZE + 2, text);
                    ctx.fill(px + PALETTE_SIZE + 1, py - 2, px + PALETTE_SIZE + 2, py + PALETTE_SIZE + 2, text);
                }
            }
            int rows = (SourVisualConfig.HITBOX_PALETTE.length + PALETTE_COLS - 1) / PALETTE_COLS;
            nextY = paletteY + rows * (PALETTE_SIZE + PALETTE_GAP) + 6;
        }

        ctx.drawText(this.textRenderer, Text.literal("Opacity"), cx, nextY, textDim, false);
        int sliderY = nextY + 12;
        ctx.fill(cx, sliderY, cx + SLIDER_W, sliderY + SLIDER_H, chip);
        int filled = (int) (SLIDER_W * (SourVisualConfig.hitboxOpacity / 100.0));
        ctx.fill(cx, sliderY, cx + filled, sliderY + SLIDER_H, accent);
        String pct = SourVisualConfig.hitboxOpacity + "%";
        ctx.drawText(this.textRenderer, Text.literal(pct), cx + SLIDER_W + 8, sliderY - 2, text, false);
    }

    // ---------- UTILITIES ----------

    private void drawUtilities(DrawContext ctx, int x, int y, int w) {
        int pad = 10;
        int cx = x + pad;
        int cy = y + pad;
        int rowW = w - pad * 2;

        int text = SourVisualConfig.getTextColor();
        int chip = SourVisualConfig.getChipColor();
        int accent = SourVisualConfig.getAccentColor();

        drawSwitchRow(ctx, cx, cy, rowW, "Fullbright", SourVisualConfig.fullbrightEnabled, chip, accent, text);
        drawSwitchRow(ctx, cx, cy + (ROW_H + ROW_GAP), rowW, "Watermark", SourVisualConfig.wmEnabled, chip, accent, text);
    }

    private void drawSwitchRow(DrawContext ctx, int x, int y, int w, String label, boolean on, int chip, int accent, int text) {
        RenderUtils.fillRounded(ctx, x, y, w, ROW_H, 4, chip);
        ctx.drawText(this.textRenderer, Text.literal(label), x + 8, y + (ROW_H - 8) / 2, text, false);

        int trackW = 22, trackH = 11;
        int trackX = x + w - trackW - 6;
        int trackY = y + (ROW_H - trackH) / 2;
        drawSwitch(ctx, trackX, trackY, trackW, trackH, on, accent);
    }

    private void drawSwitch(DrawContext ctx, int x, int y, int trackW, int trackH, boolean on, int accent) {
        int trackColor = on ? accent : 0xFF4A4A4A;
        RenderUtils.fillRounded(ctx, x, y, trackW, trackH, trackH / 2, trackColor);

        int knobD = trackH - 2;
        int knobX = on ? x + trackW - knobD - 1 : x + 1;
        int knobY = y + 1;
        RenderUtils.fillRounded(ctx, knobX, knobY, knobD, knobD, knobD / 2, 0xFFFFFFFF);
    }

    // ---------- THEMES (Settings) ----------

    private static final int THEME_COLS = 2;
    private static final int THEME_CELL_W = 104;
    private static final int THEME_CELL_GAP = 8;
    private static final int THEME_ROW_H = 26;
    private static final int THEME_BAR_H = 6;

    private void drawThemes(DrawContext ctx, int x, int y, int w, int h) {
        int pad = 10;
        int cx = x + pad;
        int cy = y + pad;

        int text = SourVisualConfig.getTextColor();
        int textDim = SourVisualConfig.getTextDimColor();

        ctx.drawText(this.textRenderer, Text.literal("Themes"), cx, cy, textDim, false);

        int gridY = cy + 14;
        ctx.enableScissor(x, gridY, x + w, y + h - 4);

        SourVisualConfig.ThemePreset[] themes = SourVisualConfig.THEMES;
        for (int i = 0; i < themes.length; i++) {
            int col = i % THEME_COLS;
            int row = i / THEME_COLS;
            int cellX = cx + col * (THEME_CELL_W + THEME_CELL_GAP);
            int cellY = gridY + row * THEME_ROW_H;

            boolean selected = SourVisualConfig.selectedThemeIndex == i;
            int nameColor = selected ? text : textDim;
            ctx.drawText(this.textRenderer, Text.literal(themes[i].name), cellX, cellY, nameColor, false);

            int barY = cellY + 10;
            ctx.fill(cellX, barY, cellX + THEME_CELL_W, barY + THEME_BAR_H, themes[i].color);

            if (selected) {
                ctx.fill(cellX - 2, barY - 2, cellX + THEME_CELL_W + 2, barY - 1, text);
                ctx.fill(cellX - 2, barY + THEME_BAR_H + 1, cellX + THEME_CELL_W + 2, barY + THEME_BAR_H + 2, text);
            }
        }

        ctx.disableScissor();
    }

    // ---------- INPUT ----------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int winW = SourVisualConfig.winW;
        int winH = SourVisualConfig.winH;

        int gripX = winX + winW - RESIZE_GRIP;
        int gripY = winY + winH - RESIZE_GRIP;
        if (mouseX >= gripX && mouseX <= winX + winW && mouseY >= gripY && mouseY <= winY + winH) {
            resizing = true;
            resizeStartMouseX = (int) mouseX;
            resizeStartMouseY = (int) mouseY;
            resizeStartW = winW;
            resizeStartH = winH;
            return true;
        }

        int sideX = winX;
        int sideY = winY + HEADER_H;
        int rowH = 24;
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            int rowY = sideY + 6 + i * rowH;
            if (mouseX >= sideX && mouseX <= sideX + SIDE_W
                    && mouseY >= rowY && mouseY <= rowY + rowH - 4) {
                currentTab = tabs[i];
                return true;
            }
        }

        if (currentTab == Tab.VISUAL) {
            if (hitboxSettingsOpen) {
                if (handleHitboxSettingsClick((int) mouseX, (int) mouseY, button)) {
                    return true;
                }
            } else if (handleVisualClick((int) mouseX, (int) mouseY, button)) {
                return true;
            }
        }

        if (currentTab == Tab.SETTINGS) {
            if (handleThemesClick((int) mouseX, (int) mouseY)) {
                return true;
            }
        }

        if (currentTab == Tab.UTILITIES) {
            if (handleUtilitiesClick((int) mouseX, (int) mouseY)) {
                return true;
            }
        }

        int wx = Watermark.lastX, wy = Watermark.lastY, ww = Watermark.lastW, wh = Watermark.lastH;
        if (mouseX >= wx && mouseX <= wx + ww && mouseY >= wy && mouseY <= wy + wh) {
            draggingWatermark = true;
            dragOffX = (int) mouseX - SourVisualConfig.wmX;
            dragOffY = (int) mouseY - SourVisualConfig.wmY;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (resizing) {
            int newW = resizeStartW + (int) (mouseX - resizeStartMouseX);
            int newH = resizeStartH + (int) (mouseY - resizeStartMouseY);
            newW = Math.max(SourVisualConfig.MIN_WIN_W, Math.min(SourVisualConfig.MAX_WIN_W, newW));
            newH = Math.max(SourVisualConfig.MIN_WIN_H, Math.min(SourVisualConfig.MAX_WIN_H, newH));
            SourVisualConfig.winW = newW;
            SourVisualConfig.winH = newH;
            return true;
        }
        if (draggingWatermark) {
            SourVisualConfig.wmX = (int) mouseX - dragOffX;
            SourVisualConfig.wmY = (int) mouseY - dragOffY;
            return true;
        }
        if (draggingOpacitySlider) {
            updateOpacityFromMouse((int) mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean wasActive = resizing || draggingWatermark || draggingOpacitySlider;
        resizing = false;
        draggingWatermark = false;
        draggingOpacitySlider = false;
        if (wasActive) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean handleVisualClick(int mouseX, int mouseY, int button) {
        int pad = 10;
        int x = winX + SIDE_W;
        int y = winY + HEADER_H;
        int cx = x + pad;
        int cy = y + pad;
        int rowW = (SourVisualConfig.winW - SIDE_W) - pad * 2;

        if (hitRow(mouseX, mouseY, cx, cy, rowW)) {
            if (button == 1) {
                hitboxSettingsOpen = true;
            } else {
                SourVisualConfig.hitboxEnabled = !SourVisualConfig.hitboxEnabled;
            }
            return true;
        }
        return false;
    }

    private boolean handleHitboxSettingsClick(int mouseX, int mouseY, int button) {
        int pad = 10;
        int x = winX + SIDE_W;
        int y = winY + HEADER_H;
        int cx = x + pad;
        int cy = y + pad;
        int rowW = (SourVisualConfig.winW - SIDE_W) - pad * 2;

        // Кнопка "назад"
        if (mouseX >= cx && mouseX <= cx + 60 && mouseY >= cy - 2 && mouseY <= cy + 10) {
            hitboxSettingsOpen = false;
            return true;
        }

        int filledY = cy + 16;
        if (hitRow(mouseX, mouseY, cx, filledY, rowW)) {
            SourVisualConfig.hitboxFilled = !SourVisualConfig.hitboxFilled;
            return true;
        }

        int colorLabelY = filledY + ROW_H + 10;
        int modeChipY = colorLabelY + 12;
        int themeChipW = this.textRenderer.getWidth("Theme") + 12;
        int customChipW = this.textRenderer.getWidth("Custom") + 12;

        if (mouseX >= cx && mouseX <= cx + themeChipW && mouseY >= modeChipY && mouseY <= modeChipY + 16) {
            SourVisualConfig.hitboxColorMode = SourVisualConfig.HitboxColorMode.THEME;
            return true;
        }
        int customChipX = cx + themeChipW + 6;
        if (mouseX >= customChipX && mouseX <= customChipX + customChipW && mouseY >= modeChipY && mouseY <= modeChipY + 16) {
            SourVisualConfig.hitboxColorMode = SourVisualConfig.HitboxColorMode.CUSTOM;
            return true;
        }

        int nextY = modeChipY + 24;
        boolean themeMode = SourVisualConfig.hitboxColorMode == SourVisualConfig.HitboxColorMode.THEME;

        if (!themeMode) {
            int paletteY = nextY;
            for (int i = 0; i < SourVisualConfig.HITBOX_PALETTE.length; i++) {
                int col = i % PALETTE_COLS;
                int row = i / PALETTE_COLS;
                int px = cx + col * (PALETTE_SIZE + PALETTE_GAP);
                int py = paletteY + row * (PALETTE_SIZE + PALETTE_GAP);
                if (mouseX >= px && mouseX <= px + PALETTE_SIZE && mouseY >= py && mouseY <= py + PALETTE_SIZE) {
                    SourVisualConfig.hitboxCustomColor = SourVisualConfig.HITBOX_PALETTE[i];
                    return true;
                }
            }
            int rows = (SourVisualConfig.HITBOX_PALETTE.length + PALETTE_COLS - 1) / PALETTE_COLS;
            nextY = paletteY + rows * (PALETTE_SIZE + PALETTE_GAP) + 6;
        }

        int sliderY = nextY + 12;
        if (mouseX >= cx - 4 && mouseX <= cx + SLIDER_W + 4
                && mouseY >= sliderY - 4 && mouseY <= sliderY + SLIDER_H + 4) {
            draggingOpacitySlider = true;
            updateOpacityFromMouse(mouseX);
            return true;
        }

        return false;
    }

    private void updateOpacityFromMouse(int mouseX) {
        int pad = 10;
        int cx = winX + SIDE_W + pad;
        int rel = mouseX - cx;
        double pct = (double) rel / SLIDER_W;
        pct = Math.max(0.0, Math.min(1.0, pct));
        SourVisualConfig.hitboxOpacity = (int) Math.round(pct * 100);
    }

    private boolean handleUtilitiesClick(int mouseX, int mouseY) {
        int pad = 10;
        int x = winX + SIDE_W;
        int y = winY + HEADER_H;
        int cx = x + pad;
        int cy = y + pad;
        int rowW = (SourVisualConfig.winW - SIDE_W) - pad * 2;

        if (hitRow(mouseX, mouseY, cx, cy, rowW)) {
            SourVisualConfig.fullbrightEnabled = !SourVisualConfig.fullbrightEnabled;
            return true;
        }

        int row2Y = cy + (ROW_H + ROW_GAP);
        if (hitRow(mouseX, mouseY, cx, row2Y, rowW)) {
            SourVisualConfig.wmEnabled = !SourVisualConfig.wmEnabled;
            return true;
        }

        return false;
    }

    private boolean hitRow(int mouseX, int mouseY, int x, int y, int w) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + ROW_H;
    }

    private boolean handleThemesClick(int mo
