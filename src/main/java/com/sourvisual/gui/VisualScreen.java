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

    private static final Identifier FONT = Identifier.of("sourvisual", "vcr_osd_mono");

    private Tab currentTab = Tab.VISUAL;

    private int winX, winY;

    private boolean draggingWatermark = false;
    private int dragOffX, dragOffY;

    private boolean resizing = false;
    private int resizeStartMouseX, resizeStartMouseY;
    private int resizeStartW, resizeStartH;

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
            case UTILITIES -> drawPlaceholder(ctx, x, y, "Utilities modules go here");
            case KEYBINDS -> drawPlaceholder(ctx, x, y, "Keybind list goes here");
            case SETTINGS -> drawSettings(ctx, x, y, mouseX, mouseY);
        }
    }

    private void drawPlaceholder(DrawContext ctx, int x, int y, String text) {
        ctx.drawText(this.textRenderer, label(text), x + 10, y + 10, SourVisualConfig.getTextDimColor(), false);
    }

    // ---------- SETTINGS ----------

    private static final String[] THEME_NAMES = {"White", "Dark", "Dark+", "Peach", "Custom"};

    private void drawSettings(DrawContext ctx, int x, int y, int mouseX, int mouseY) {
        int pad = 10;
        int cx = x + pad;
        int cy = y + pad;

        int text = SourVisualConfig.getTextColor();
        int textDim = SourVisualConfig.getTextDimColor();
        int chip = SourVisualConfig.getChipColor();
        int chipOn = SourVisualConfig.getChipOnColor();
        int accent = SourVisualConfig.getAccentColor();

        // Themes
        ctx.drawText(this.textRenderer, label("Themes"), cx, cy, textDim, false);
        int themeChipY = cy + 12;
        int chipX = cx;
        for (int i = 0; i < THEME_NAMES.length; i++) {
            int chipW = this.textRenderer.getWidth(THEME_NAMES[i]) + 12;
            boolean active = SourVisualConfig.theme.ordinal() == i;
            ctx.fill(chipX, themeChipY, chipX + chipW, themeChipY + 16, active ? chipOn : chip);
            ctx.drawText(this.textRenderer, label(THEME_NAMES[i]), chipX + 6, themeChipY + 4, text, false);
            chipX += chipW + 6;
        }

        // Customizing
        int custY = themeChipY + 26;
        ctx.drawText(this.textRenderer, label("Customizing"), cx, custY, textDim, false);
        int rowY = custY + 12;
        drawColorRow(ctx, cx, rowY, true, chip, text);
        drawColorRow(ctx, cx + 118, rowY, false, chip, text);

        // RGB Mode
        int modeY = rowY + 24;
        ctx.drawText(this.textRenderer, label("RGB Mode"), cx, modeY, textDim, false);
        String[] modes = {"Radial", "Sphere", "Metric"};
        int modeChipY = modeY + 12;
        int mChipX = cx;
        for (int i = 0; i < modes.length; i++) {
            int chipW = this.textRenderer.getWidth(modes[i]) + 12;
            boolean active = SourVisualConfig.rgbMode.ordinal() == i;
            ctx.fill(mChipX, modeChipY, mChipX + chipW, modeChipY + 16, active ? chipOn : chip);
            ctx.drawText(this.textRenderer, label(modes[i]), mChipX + 6, modeChipY + 4, text, false);
            mChipX += chipW + 6;
        }

        // Watermark toggle
        int wmY = modeChipY + 26;
        ctx.drawText(this.textRenderer, label("Watermark"), cx, wmY, textDim, false);
        int r1Y = wmY + 14;
        drawToggleRow(ctx, cx, r1Y, "Show watermark", SourVisualConfig.wmEnabled, chip, accent, text);

        int hintY = r1Y + 20;
        ctx.drawText(this.textRenderer,
                label("Drag watermark with mouse while menu is open"),
                cx, hintY, textDim, false);

        int hint2Y = hintY + 12;
        ctx.drawText(this.textRenderer,
                label("Drag bottom-right corner to resize menu"),
                cx, hint2Y, textDim, false);
    }

    private void drawColorRow(DrawContext ctx, int x, int y, boolean first, int chip, int text) {
        int chipW = 20, chipH = 14, gap = 4;
        int r = first ? SourVisualConfig.color1R : SourVisualConfig.color2R;
        int g = first ? SourVisualConfig.color1G : SourVisualConfig.color2G;
        int b = first ? SourVisualConfig.color1B : SourVisualConfig.color2B;

        drawNumChip(ctx, x, y, chipW, chipH, r, chip, text);
        drawNumChip(ctx, x + chipW + gap, y, chipW, chipH, g, chip, text);
        drawNumChip(ctx, x + (chipW + gap) * 2, y, chipW, chipH, b, chip, text);

        int swatchX = x + (chipW + gap) * 3;
        int color = first ? SourVisualConfig.getAccentColor() : SourVisualConfig.getSecondaryColor();
        ctx.fill(swatchX, y, swatchX + chipH, y + chipH, color);
    }

    private void drawNumChip(DrawContext ctx, int x, int y, int w, int h, int value, int chip, int text) {
        ctx.fill(x, y, x + w, y + h, chip);
        String s = String.valueOf(value);
        int tw = this.textRenderer.getWidth(s);
        ctx.drawText(this.textRenderer, label(s), x + (w - tw) / 2, y + 3, text, false);
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
            if (handleSettingsClick((int) mouseX, (int) mouseY, button)) {
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
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean wasActive = resizing || draggingWatermark;
        resizing = false;
        draggingWatermark = false;
        if (wasActive) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean handleSettingsClick(int mouseX, int mouseY, int button) {
        int pad = 10;
        int x = winX + SIDE_W;
        int y = winY + HEADER_H;
        int cx = x + pad;
        int cy = y + pad;

        // Themes
        int themeChipY = cy + 12;
        int chipX = cx;
        for (int i = 0; i < THEME_NAMES.length; i++) {
            int chipW = this.textRenderer.getWidth(THEME_NAMES[i]) + 12;
            if (mouseX >= chipX && mouseX <= chipX + chipW && mouseY >= themeChipY && mouseY <= themeChipY + 16) {
                SourVisualConfig.theme = SourVisualConfig.Theme.values()[i];
                return true;
            }
            chipX += chipW + 6;
        }

        // Customizing (RGB чипы: ЛКМ +5, ПКМ -5)
        int custY = themeChipY + 26;
        int rowY = custY + 12;
        if (handleColorRowClick(mouseX, mouseY, cx, rowY, button, true)) return true;
        if (handleColorRowClick(mouseX, mouseY, cx + 118, rowY, button, false)) return true;

        // RGB Mode
        int modeY = rowY + 24;
        String[] modes = {"Radial", "Sphere", "Metric"};
        int modeChipY = modeY + 12;
        int mChipX = cx;
        for (int i = 0; i < modes.length; i++) {
            int chipW = this.textRenderer.getWidth(modes[i]) + 12;
            if (mouseX >= mChipX && mouseX <= mChipX + chipW && mouseY >= modeChipY && mouseY <= modeChipY + 16) {
                SourVisualConfig.rgbMode = SourVisualConfig.RgbMode.values()[i];
                return true;
            }
            mChipX += chipW + 6;
        }

        // Watermark toggle
        int wmY = modeChipY + 26;
        int r1Y = wmY + 14;
        if (hitToggle(mouseX, mouseY, cx, r1Y, "Show watermark")) {
            SourVisualConfig.wmEnabled = !SourVisualConfig.wmEnabled;
            return true;
        }

        return false;
    }

    private boolean hitToggle(int mouseX, int mouseY, int x, int y, String name) {
        int boxX = x + this.textRenderer.getWidth(name) + 6;
        int boxSize = 10;
        return mouseX >= boxX && mouseX <= boxX + boxSize && mouseY >= y - 1 && mouseY <= y - 1 + boxSize;
    }

    private boolean handleColorRowClick(int mouseX, int mouseY, int x, int y, int button, boolean first) {
        int chipW = 20, chipH = 14, gap = 4;
        int delta = button == 0 ? 5 : (button == 1 ? -5 : 0);
        if (delta == 0) return false;

        for (int i = 0; i < 3; i++) {
            int cx = x + i * (chipW + gap);
            if (mouseX >= cx && mouseX <= cx + chipW && mouseY >= y && mouseY <= y + chipH) {
                applyColorDelta(first, i, delta);
                return true;
            }
        }
        return false;
    }

    private void applyColorDelta(boolean first, int channel, int delta) {
        if (first) {
            switch (channel) {
                case 0 -> SourVisualConfig.color1R = clamp(SourVisualConfig.color1R + delta);
                case 1 -> SourVisualConfig.color1G = clamp(SourVisualConfig.color1G + delta);
                case 2 -> SourVisualConfig.color1B = clamp(SourVisualConfig.color1B + delta);
            }
        } else {
            switch (channel) {
                case 0 -> SourVisualConfig.color2R = clamp(SourVisualConfig.color2R + delta);
                case 1 -> SourVisualConfig.color2G = clamp(SourVisualConfig.color2G + delta);
                case 2 -> SourVisualConfig.color2B = clamp(SourVisualConfig.color2B + delta);
            }
        }
        SourVisualConfig.theme = SourVisualConfig.Theme.CUSTOM;
    }

    private int clamp(int v) {
        return Math.max(0, Math.min(255, v));
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
