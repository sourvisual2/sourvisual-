package com.sourvisual.gui;

import com.sourvisual.config.SourVisualConfig;
import com.sourvisual.hud.Watermark;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class VisualScreen extends Screen {

    private static final int SIDE_W   = 82;
    private static final int HEADER_H = 27;
    private static final int R        = 6;
    private static final int RESIZE_GRIP = 10;

    private static final Identifier FONT = Identifier.of("sourvisual", "minecraftia");

    private Tab currentTab = Tab.VISUAL;

    private int winX, winY;

    private boolean draggingWatermark = false;
    private int dragOffX, dragOffY;

    private boolean resizing = false;
    private int resizeStartMouseX, resizeStartMouseY;
    private int resizeStartW, resizeStartH;

    private boolean draggingSlider = false;

    public VisualScreen() {
        super(Text.literal("Sour Visual"));
    }

    @Override
    protected void init() {
        winX = (this.width - SourVisualConfig.winW) / 2;
        winY = (this.height - SourVisualConfig.winH) / 2;
    }

    // Полностью отключаем системный блюр фона для этого экрана
    @Override
    protected void applyBlur(float delta) {
        // ничего не делаем — блюр не применяется
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

        // Шапка
        ctx.fill(winX, winY, winX + winW, winY + HEADER_H, header);
        ctx.drawText(this.textRenderer, label("sour visual"), winX + 10, winY + 9, text, false);

        // Сайдбар
        int sideX = winX;
        int sideY = winY + HEADER_H;
        ctx.fill(sideX, sideY, sideX + SIDE_W, winY + winH, side);

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
            ctx.drawText(this.textRenderer, label(tab.label), sideX + 12, rowY + 8, textColor, false);
        }

        // Контентная область
        int contentX = winX + SIDE_W;
        int contentY = winY + HEADER_H;
        int contentW = winW - SIDE_W;
        int contentH = winH - HEADER_H;
        renderTabContent(ctx, contentX, contentY, contentW, contentH, mouseX, mouseY);

        // Ручка изменения размера в правом нижнем углу
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
            case VISUAL -> drawPlaceholder(ctx, x, y, "Visual modules go here");
            case UTILITIES -> drawUtilities(ctx, x, y);
            case KEYBINDS -> drawPlaceholder(ctx, x, y, "Keybind list goes here");
            case SETTINGS -> drawThemes(ctx, x, y, w, h);
        }
    }

    private void drawPlaceholder(DrawContext ctx, int x, int y, String text) {
        ctx.drawText(this.textRenderer, label(text), x + 10, y + 10, SourVisualConfig.getTextDimColor(), false);
    }

    // ---------- UTILITIES ----------

    private static final int SLIDER_W = 180;
    private static final int SLIDER_H = 6;

    private void drawUtilities(DrawContext ctx, int x, int y) {
        int pad = 10;
        int cx = x + pad;
        int cy = y + pad;

        int text = SourVisualConfig.getTextColor();
        int textDim = SourVisualConfig.getTextDimColor();
        int chip = SourVisualConfig.getChipColor();
        int accent = SourVisualConfig.getAccentColor();

        ctx.drawText(this.textRenderer, label("Fullbright"), cx, cy, textDim, false);

        int toggleY = cy + 14;
        drawToggleRow(ctx, cx, toggleY, "Enabled", SourVisualConfig.fullbrightEnabled, chip, accent, text);

        int sliderY = toggleY + 20;
        ctx.fill(cx, sliderY, cx + SLIDER_W, sliderY + SLIDER_H, chip);
        int filled = (int) (SLIDER_W * (SourVisualConfig.fullbrightValue / 100.0));
        ctx.fill(cx, sliderY, cx + filled, sliderY + SLIDER_H, accent);

        String pct = SourVisualConfig.fullbrightValue + "%";
        ctx.drawText(this.textRenderer, label(pct), cx + SLIDER_W + 8, sliderY - 2, text, false);

        int wmLabelY = sliderY + 20;
        ctx.drawText(this.textRenderer, label("Watermark"), cx, wmLabelY, textDim, false);
        int wmToggleY = wmLabelY + 14;
        drawToggleRow(ctx, cx, wmToggleY, "Show watermark", SourVisualConfig.wmEnabled, chip, accent, text);
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

        ctx.drawText(this.textRenderer, label("Themes"), cx, cy, textDim, false);

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
            ctx.drawText(this.textRenderer, label(themes[i].name), cellX, cellY, nameColor, false);

            int barY = cellY + 10;
            ctx.fill(cellX, barY, cellX + THEME_CELL_W, barY + THEME_BAR_H, themes[i].color);

            if (selected) {
                ctx.fill(cellX - 2, barY - 2, cellX + THEME_CELL_W + 2, barY - 1, text);
                ctx.fill(cellX - 2, barY + THEME_BAR_H + 1, cellX + THEME_CELL_W + 2, barY + THEME_BAR_H + 2, text);
            }
        }

        ctx.disableScissor();
    }

    private void drawToggleRow(DrawContext ctx, int x, int y, String name, boolean on, int chip, int accent, int text) {
        ctx.drawText(this.textRenderer, label(name), x, y, text, false);
        int boxX = x + this.textRenderer.getWidth(name) + 6;
        int boxSize = 10;
        ctx.fill(boxX, y - 1, boxX + boxSize, y - 1 + boxSize, on ? accent : chip);
        if (on) {
            ctx.drawText(this.textRenderer, label("v"), boxX + 1, y - 1, 0xFFFFFFFF, false);
        }
    }

    private Text label(String s) {
        return Text.literal(s).setStyle(Text.empty().getStyle().withFont(FONT));
    }

    // ---------- INPUT ----------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int winW = SourVisualConfig.winW;
        int winH = SourVisualConfig.winH;

        // Ручка ресайза — проверяем первой
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

        // Начало драга вотермарки
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
        if (draggingSlider) {
            updateSliderFromMouse((int) mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean wasActive = resizing || draggingWatermark || draggingSlider;
        resizing = false;
        draggingWatermark = false;
        draggingSlider = false;
        if (wasActive) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean handleUtilitiesClick(int mouseX, int mouseY) {
        int pad = 10;
        int x = winX + SIDE_W;
        int y = winY + HEADER_H;
        int cx = x + pad;
        int cy = y + pad;

        int toggleY = cy + 14;
        if (hitToggle(mouseX, mouseY, cx, toggleY, "Enabled")) {
            SourVisualConfig.fullbrightEnabled = !SourVisualConfig.fullbrightEnabled;
            return true;
        }

        int sliderY = toggleY + 20;
        if (mouseX >= cx - 4 && mouseX <= cx + SLIDER_W + 4
                && mouseY >= sliderY - 4 && mouseY <= sliderY + SLIDER_H + 4) {
            draggingSlider = true;
            updateSliderFromMouse(mouseX);
            return true;
        }

        int wmLabelY = sliderY + 20;
        int wmToggleY = wmLabelY + 14;
        if (hitToggle(mouseX, mouseY, cx, wmToggleY, "Show watermark")) {
            SourVisualConfig.wmEnabled = !SourVisualConfig.wmEnabled;
            return true;
        }

        return false;
    }

    private void updateSliderFromMouse(int mouseX) {
        int pad = 10;
        int cx = winX + SIDE_W + pad;
        int rel = mouseX - cx;
        double pct = (double) rel / SLIDER_W;
        pct = Math.max(0.0, Math.min(1.0, pct));
        SourVisualConfig.fullbrightValue = (int) Math.round(pct * 100);
    }

    private boolean handleThemesClick(int mouseX, int mouseY) {
        int pad = 10;
        int x = winX + SIDE_W;
        int y = winY + HEADER_H;
        int cx = x + pad;
        int cy = y + pad;
        int gridY = cy + 14;

        SourVisualConfig.ThemePreset[] themes = SourVisualConfig.THEMES;
        for (int i = 0; i < themes.length; i++) {
            int col = i % THEME_COLS;
            int row = i / THEME_COLS;
            int cellX = cx + col * (THEME_CELL_W + THEME_CELL_GAP);
            int cellY = gridY + row * THEME_ROW_H;

            if (mouseX >= cellX && mouseX <= cellX + THEME_CELL_W
                    && mouseY >= cellY && mouseY <= cellY + THEME_ROW_H - 4) {
                SourVisualConfig.selectedThemeIndex = i;
                return true;
            }
        }
        return false;
    }

    private boolean hitToggle(int mouseX, int mouseY, int x, int y, String name) {
        int boxX = x + this.textRenderer.getWidth(name) + 6;
        int boxSize = 10;
        return mouseX >= boxX && mouseX <= boxX + boxSize && mouseY >= y - 1 && mouseY <= y - 1 + boxSize;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
                }
