package com.sourvisual.gui;

import com.sourvisual.config.SourVisualConfig;
import com.sourvisual.hud.Watermark;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class VisualScreen extends Screen {

    // Размеры главного окна
    private static final int WIN_W    = 315;
    private static final int WIN_H    = 195;
    private static final int SIDE_W   = 82;
    private static final int HEADER_H = 27;
    private static final int R        = 6;

    private static final Identifier FONT = Identifier.of("sourvisual", "league_spartan");

    private static final int COLOR_BG       = 0xF0141414;
    private static final int COLOR_HEADER   = 0xF01B1B1B;
    private static final int COLOR_SIDE     = 0xF0181818;
    private static final int COLOR_ACCENT   = 0xFF7C5CFF;
    private static final int COLOR_TEXT     = 0xFFE0E0E0;
    private static final int COLOR_TEXT_DIM = 0xFF8A8A8A;
    private static final int COLOR_CHIP     = 0xFF232323;
    private static final int COLOR_CHIP_ON  = 0xFF2E2350;

    private Tab currentTab = Tab.VISUAL;

    private int winX, winY;

    private boolean draggingWatermark = false;
    private int dragOffX, dragOffY;

    public VisualScreen() {
        super(Text.literal("Sour Visual"));
    }

    @Override
    protected void init() {
        winX = (this.width - WIN_W) / 2;
        winY = (this.height - WIN_H) / 2;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);

        RenderUtils.fillRounded(ctx, winX, winY, WIN_W, WIN_H, R, COLOR_BG);

        // Шапка
        ctx.fill(winX, winY, winX + WIN_W, winY + HEADER_H, COLOR_HEADER);
        ctx.drawText(this.textRenderer,
                label("sour visual"),
                winX + 10, winY + 9, COLOR_TEXT, false);

        // Сайдбар
        int sideX = winX;
        int sideY = winY + HEADER_H;
        ctx.fill(sideX, sideY, sideX + SIDE_W, winY + WIN_H, COLOR_SIDE);

        Tab[] tabs = Tab.values();
        int rowH = 24;
        for (int i = 0; i < tabs.length; i++) {
            Tab tab = tabs[i];
            int rowY = sideY + 6 + i * rowH;
            boolean active = tab == currentTab;
            boolean hovered = mouseX >= sideX && mouseX <= sideX + SIDE_W
                    && mouseY >= rowY && mouseY <= rowY + rowH - 4;

            if (active) {
                ctx.fill(sideX, rowY, sideX + 3, rowY + rowH - 4, COLOR_ACCENT);
            }
            int textColor = active ? COLOR_TEXT : (hovered ? COLOR_TEXT : COLOR_TEXT_DIM);
            ctx.drawText(this.textRenderer,
                    label(tab.label),
                    sideX + 12, rowY + 8, textColor, false);
        }

        // Контентная область
        int contentX = winX + SIDE_W;
        int contentY = winY + HEADER_H;
        int contentW = WIN_W - SIDE_W;
        int contentH = WIN_H - HEADER_H;
        renderTabContent(ctx, contentX, contentY, contentW, contentH, mouseX, mouseY);

        super.render(ctx, mouseX, mouseY, delta);
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
        ctx.drawText(this.textRenderer,
                label(text),
                x + 10, y + 10, COLOR_TEXT_DIM, false);
    }

    // ---------- SETTINGS ----------

    private static final String[] THEME_NAMES = {"White", "Dark", "Dark+", "Peach", "Custom"};

    private void drawSettings(DrawContext ctx, int x, int y, int mouseX, int mouseY) {
        int pad = 10;
        int cx = x + pad;
        int cy = y + pad;

        // Themes
        ctx.drawText(this.textRenderer, label("Themes"), cx, cy, COLOR_TEXT_DIM, false);
        int themeChipY = cy + 12;
        int chipX = cx;
        for (int i = 0; i < THEME_NAMES.length; i++) {
            int chipW = this.textRenderer.getWidth(THEME_NAMES[i]) + 12;
            boolean active = SourVisualConfig.theme.ordinal() == i;
            ctx.fill(chipX, themeChipY, chipX + chipW, themeChipY + 16, active ? COLOR_CHIP_ON : COLOR_CHIP);
            ctx.drawText(this.textRenderer, label(THEME_NAMES[i]), chipX + 6, themeChipY + 4, COLOR_TEXT, false);
            chipX += chipW + 6;
        }

        // Customizing
        int custY = themeChipY + 26;
        ctx.drawText(this.textRenderer, label("Customizing"), cx, custY, COLOR_TEXT_DIM, false);
        int rowY = custY + 12;
        drawColorRow(ctx, cx, rowY, true);
        drawColorRow(ctx, cx + 118, rowY, false);

        // RGB Mode
        int modeY = rowY + 24;
        ctx.drawText(this.textRenderer, label("RGB Mode"), cx, modeY, COLOR_TEXT_DIM, false);
        String[] modes = {"Radial", "Sphere", "Metric"};
        int modeChipY = modeY + 12;
        int mChipX = cx;
        for (int i = 0; i < modes.length; i++) {
            int chipW = this.textRenderer.getWidth(modes[i]) + 12;
            boolean active = SourVisualConfig.rgbMode.ordinal() == i;
            ctx.fill(mChipX, modeChipY, mChipX + chipW, modeChipY + 16, active ? COLOR_CHIP_ON : COLOR_CHIP);
            ctx.drawText(this.textRenderer, label(modes[i]), mChipX + 6, modeChipY + 4, COLOR_TEXT, false);
            mChipX += chipW + 6;
        }

        // Watermark toggles
        int wmY = modeChipY + 26;
        ctx.drawText(this.textRenderer, label("Watermark"), cx, wmY, COLOR_TEXT_DIM, false);
        int col1X = cx;
        int col2X = cx + 90;
        int col3X = cx + 170;
        int r1Y = wmY + 14;
        int r2Y = r1Y + 16;

        drawToggleRow(ctx, col1X, r1Y, "Logo", SourVisualConfig.wmLogo);
        drawToggleRow(ctx, col2X, r1Y, "Title", SourVisualConfig.wmTitle);
        drawToggleRow(ctx, col3X, r1Y, "Nickname", SourVisualConfig.wmNickname);

        drawToggleRow(ctx, col1X, r2Y, "FPS", SourVisualConfig.wmFps);
        drawToggleRow(ctx, col2X, r2Y, "Ping", SourVisualConfig.wmPing);
        drawToggleRow(ctx, col3X, r2Y, "Server", SourVisualConfig.wmServer);

        int hintY = r2Y + 20;
        ctx.drawText(this.textRenderer,
                label("Drag watermark with mouse while menu is open"),
                cx, hintY, COLOR_TEXT_DIM, false);
    }

    private void drawColorRow(DrawContext ctx, int x, int y, boolean first) {
        int chipW = 20, chipH = 14, gap = 4;
        int r = first ? SourVisualConfig.color1R : SourVisualConfig.color2R;
        int g = first ? SourVisualConfig.color1G : SourVisualConfig.color2G;
        int b = first ? SourVisualConfig.color1B : SourVisualConfig.color2B;

        drawNumChip(ctx, x, y, chipW, chipH, r);
        drawNumChip(ctx, x + chipW + gap, y, chipW, chipH, g);
        drawNumChip(ctx, x + (chipW + gap) * 2, y, chipW, chipH, b);

        int swatchX = x + (chipW + gap) * 3;
        int color = first ? SourVisualConfig.getAccentColor() : SourVisualConfig.getSecondaryColor();
        ctx.fill(swatchX, y, swatchX + chipH, y + chipH, color);
    }

    private void drawNumChip(DrawContext ctx, int x, int y, int w, int h, int value) {
        ctx.fill(x, y, x + w, y + h, COLOR_CHIP);
        String s = String.valueOf(value);
        int tw = this.textRenderer.getWidth(s);
        ctx.drawText(this.textRenderer, label(s), x + (w - tw) / 2, y + 3, COLOR_TEXT, false);
    }

    private void drawToggleRow(DrawContext ctx, int x, int y, String name, boolean on) {
        ctx.drawText(this.textRenderer, label(name), x, y, COLOR_TEXT, false);
        int boxX = x + this.textRenderer.getWidth(name) + 6;
        int boxSize = 10;
        ctx.fill(boxX, y - 1, boxX + boxSize, y - 1 + boxSize, on ? COLOR_ACCENT : COLOR_CHIP);
        if (on) {
            ctx.drawText(this.textRenderer, Text.literal("v"), boxX + 1, y - 1, 0xFFFFFFFF, false);
        }
    }

    private Text label(String s) {
        return Text.literal(s).setStyle(Text.empty().getStyle().withFont(FONT));
    }

    // ---------- INPUT ----------

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        int sideX = winX;
        int sideY = winY + HEADER_H;
        int rowH = 24;
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            int rowY = sideY + 6 + i * rowH;
            if (mouseX >= sideX && mouseX <= sideX + SIDE_W
                    && mouseY >= rowY && mouseY <= rowY + rowH - 4) {
                currentTab = tabs[i];
                super.mouseClicked(mouseX, mouseY, button);
                return;
            }
        }

        if (currentTab == Tab.SETTINGS) {
            if (handleSettingsClick((int) mouseX, (int) mouseY, button)) {
                super.mouseClicked(mouseX, mouseY, button);
                return;
            }
        }

        // Начало драга вотермарки
        int wx = Watermark.lastX, wy = Watermark.lastY, ww = Watermark.lastW, wh = Watermark.lastH;
        if (mouseX >= wx && mouseX <= wx + ww && mouseY >= wy && mouseY <= wy + wh) {
            draggingWatermark = true;
            dragOffX = (int) mouseX - SourVisualConfig.wmX;
            dragOffY = (int) mouseY - SourVisualConfig.wmY;
            return;
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingWatermark) {
            SourVisualConfig.wmX = (int) mouseX - dragOffX;
            SourVisualConfig.wmY = (int) mouseY - dragOffY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        draggingWatermark = false;
        super.mouseReleased(mouseX, mouseY, button);
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

        // Watermark toggles
        int wmY = modeChipY + 26;
        int col1X = cx, col2X = cx + 90, col3X = cx + 170;
        int r1Y = wmY + 14, r2Y = r1Y + 16;

        if (hitToggle(mouseX, mouseY, col1X, r1Y, "Logo")) { SourVisualConfig.wmLogo = !SourVisualConfig.wmLogo; return true; }
        if (hitToggle(mouseX, mouseY, col2X, r1Y, "Title")) { SourVisualConfig.wmTitle = !SourVisualConfig.wmTitle; return true; }
        if (hitToggle(mouseX, mouseY, col3X, r1Y, "Nickname")) { SourVisualConfig.wmNickname = !SourVisualConfig.wmNickname; return true; }
        if (hitToggle(mouseX, mouseY, col1X, r2Y, "FPS")) { SourVisualConfig.wmFps = !SourVisualConfig.wmFps; return true; }
        if (hitToggle(mouseX, mouseY, col2X, r2Y, "Ping")) { SourVisualConfig.wmPing = !SourVisualConfig.wmPing; return true; }
        if (hitToggle(mouseX, mouseY, col3X, r2Y, "Server")) { SourVisualConfig.wmServer = !SourVisualConfig.wmServer; return true; }

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
