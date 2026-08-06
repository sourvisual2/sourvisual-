package com.sourvisual.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class VisualMenuScreen extends Screen {

    private static final int WIN_W     = 600;
    private static final int WIN_H     = 390;
    private static final int SIDE_W    = 155;
    private static final int HEADER_H  = 44;

    private static final int C_BG          = 0xEE0D0D12;
    private static final int C_BORDER      = 0xFF252530;
    private static final int C_DIVIDER     = 0xFF1E1E28;
    private static final int C_SEL_BG      = 0x33FFFFFF;
    private static final int C_WHITE       = 0xFFFFFFFF;
    private static final int C_DIM         = 0xFF888899;
    private static final int C_ACCENT1     = 0xFF9B6FFF;
    private static final int C_ACCENT2     = 0xFFCC99FF;
    private static final int C_HOVER       = 0xFFCCCCDD;

    private int wX, wY;
    private TextFieldWidget searchField;
    private MenuCategory selected = MenuCategory.EFFECTS;

    public VisualMenuScreen() {
        super(Text.literal("Sour Visual"));
    }

    @Override
    protected void init() {
        wX = (this.width  - WIN_W) / 2;
        wY = (this.height - WIN_H) / 2;

        searchField = new TextFieldWidget(
                this.textRenderer,
                wX + SIDE_W + 16,
                wY + 13,
                WIN_W - SIDE_W - 56,
                18,
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

        // фон окна
        fill(ctx, wX, wY, WIN_W, WIN_H, C_BG);
        border(ctx, wX, wY, WIN_W, WIN_H, C_BORDER);

        // сайдбар
        fill(ctx, wX, wY, SIDE_W, WIN_H, C_BG);
        ctx.drawVerticalLine(wX + SIDE_W, wY, wY + WIN_H, C_BORDER);

        // заголовок
        int p1w = this.textRenderer.getWidth("Sour ");
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Sour "), wX + 16, wY + 15, C_ACCENT1);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Visual"), wX + 16 + p1w, wY + 15, C_ACCENT2);

        // разделитель шапки
        ctx.drawHorizontalLine(wX, wX + WIN_W, wY + HEADER_H, C_DIVIDER);
        ctx.drawVerticalLine(wX + SIDE_W, wY, wY + HEADER_H, C_BORDER);

        // поиск
        border(ctx, wX + SIDE_W + 10, wY + 12, WIN_W - SIDE_W - 20, 20, C_DIVIDER);
        searchField.render(ctx, mx, my, delta);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("⌕"),
                wX + WIN_W - 22, wY + 16, C_DIM);

        // категории
        int catY = wY + HEADER_H + 14;
        for (MenuCategory cat : MenuCategory.values()) {
            boolean hov = mx >= wX + 6 && mx <= wX + SIDE_W - 6
                       && my >= catY   && my <= catY + 24;
            boolean sel = cat == selected;

            if (sel) fill(ctx, wX + 6, catY, SIDE_W - 12, 24, C_SEL_BG);

            int col = sel ? C_WHITE : (hov ? C_HOVER : C_DIM);
            ctx.drawTextWithShadow(this.textRenderer,
                    Text.literal(iconFor(cat)), wX + 14, catY + 7, col);
            ctx.drawTextWithShadow(this.textRenderer,
                    Text.literal(cat.label), wX + 30, catY + 7, col);

            catY += 36;
        }

        // контент
        renderContent(ctx, wX + SIDE_W + 16, wY + HEADER_H + 16, mx, my);
    }

    private void renderContent(DrawContext ctx, int x, int y, int mx, int my) {
        switch (selected) {
            case EFFECTS -> {
                title(ctx, x, y, "» Effects");
                toggle(ctx, x, y + 20,  "Night Vision",    false);
                toggle(ctx, x, y + 44,  "Speed",           false);
                toggle(ctx, x, y + 68,  "Jump Boost",      false);
                toggle(ctx, x, y + 92,  "Regeneration",    false);
                toggle(ctx, x, y + 116, "Strength",        false);
                toggle(ctx, x, y + 140, "Water Breathing", false);
            }
            case VISUAL -> {
                title(ctx, x, y, "» Visual");
                toggle(ctx, x, y + 20,  "FullBright",  false);
                toggle(ctx, x, y + 44,  "ESP Players", false);
                toggle(ctx, x, y + 68,  "ESP Mobs",    false);
                toggle(ctx, x, y + 92,  "No Weather",  false);
                toggle(ctx, x, y + 116, "No Fog",      false);
                toggle(ctx, x, y + 140, "Tracers",     false);
            }
            case SETTINGS -> {
                title(ctx, x, y, "» Settings");
                toggle(ctx, x, y + 20, "Open: Right Shift", true);
                toggle(ctx, x, y + 44, "Hide HUD in menu",  false);
                toggle(ctx, x, y + 68, "Show FPS",          false);
                info(ctx, x, y + 105, "Mod version: 1.0.0");
                info(ctx, x, y + 120, "MC version:  1.21");
            }
        }
    }

    private void title(DrawContext ctx, int x, int y, String t) {
        ctx.drawTextWithShadow(this.textRenderer, Text.literal(t), x, y, C_ACCENT1);
    }

    private void toggle(DrawContext ctx, int x, int y, String label, boolean on) {
        ctx.drawTextWithShadow(this.textRenderer, Text.literal(label), x, y, C_WHITE);
        int tx = x + 280, ty = y - 2;
        fill(ctx, tx, ty, 26, 12, on ? 0xFF5A3FBF : 0xFF333340);
        border(ctx, tx, ty, 26, 12, C_BORDER);
        int cx = on ? tx + 16 : tx + 2;
        fill(ctx, cx, ty + 2, 8, 8, on ? C_ACCENT2 : C_DIM);
    }

    private void info(DrawContext ctx, int x, int y, String t) {
        ctx.drawTextWithShadow(this.textRenderer, Text.literal(t), x, y, C_DIM);
    }

    private String iconFor(MenuCategory cat) {
        return switch (cat) {
            case EFFECTS  -> "★";
            case VISUAL   -> "◉";
            case SETTINGS -> "⚙";
        };
    }

    private void fill(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + h, color);
    }

    private void border(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.drawHorizontalLine(x, x + w, y,     color);
        ctx.drawHorizontalLine(x, x + w, y + h, color);
        ctx.drawVerticalLine(x,     y, y + h, color);
        ctx.drawVerticalLine(x + w, y, y + h, color);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int catY = wY + HEADER_H + 14;
        for (MenuCategory cat : MenuCategory.values()) {
            if (mx >= wX + 6 && mx <= wX + SIDE_W - 6
             && my >= catY   && my <= catY + 24) {
                selected = cat;
                return true;
            }
            catY += 36;
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
          }
