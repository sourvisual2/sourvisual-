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
    }

    private void renderContent(DrawContext ctx, int x, int y, int mx, int my) {
        switch (selected) {
            case VISUAL -> {
                drawToggleRow(ctx, x, y,
                        "Hit Color",
                        "Фиолетовый цвет при ударе.",
                        ModConfig.hitColorEnabled, mx, my, 0);
            }
            case EFFECTS -> ctx.drawTextWithShadow(
                    this.textRenderer, Text.literal("— empty —"), x, y, C_DIM);
            case SETTINGS -> ctx.drawTextWithShadow(
                    this.textRenderer, Text.literal("— empty —"), x, y, C_DIM);
        }
    }

    private void drawToggleRow(DrawContext ctx, int x, int y,
                               String title, String desc,
                               boolean on, int mx, int my, int index) {
        int rowW = WIN_W - SIDE_W - 12;
        int rowH = 34;
        int ry   = y + index * (rowH + 4);

        ctx.fill(x - 2, ry, x + rowW, ry + rowH, 0x22FFFFFF);
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal(title), x + 3, ry + 6, C_WHITE);
        ctx.drawTextWithShadow(this.textRenderer,
                Text.literal(desc), x + 3, ry + 18, C_DIM);

        int tx = x + rowW - 24;
        int ty = ry + 10;
        ctx.fill(tx, ty, tx + 20, ty + 12, on ? 0xFF5A3FBF : 0xFF333340);
        borderRounded(ctx, tx, ty, 20, 12, 3, C_BORDER);
        int cx = on ? tx + 12 : tx + 2;
        ctx.fill(cx, ty + 2, cx + 8, ty + 10, on ? C_ACCENT2 : C_DIM);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
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

        // тумблер Hit Color
        if (selected == MenuCategory.VISUAL) {
            int x    = wX + SIDE_W + 8;
            int y    = wY + HEADER_H + 8;
            int rowW = WIN_W - SIDE_W - 12;
            int tx   = x + rowW - 24;
            int ty   = y + 10;
            if (mx >= tx && mx <= tx + 20 && my >= ty && my <= ty + 12) {
                ModConfig.hitColorEnabled = !ModConfig.hitColorEnabled;
                ModConfig.save();
                return true;
            }
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

    private void fillRounded(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        ctx.fill(x + r, y,     x + w - r, y + h,     color);
        ctx.fill(x,     y + r, x + r,     y + h - r, color);
        ctx.fill(x + w - r, y + r, x + w, y + h - r, color);
        fillCorner(ctx, x,         y,         r,  1,  1, color);
        fillCorner(ctx, x + w - r, y,         r, -1,  1, color);
        fillCorner(ctx, x,         y + h - r, r,  1, -1, color);
        fillCorner(ctx, x + w - r, y + h - r, r, -1, -1, color);
    }

    private void fillCorner(DrawContext ctx, int cx, int cy, int r,
                            int dx, int dy, int color) {
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < r; j++) {
                double dist = Math.sqrt((r-1-i)*(r-1-i)+(r-1-j)*(r-1-j));
                if (dist < r - 0.5) {
                    int px = cx + (dx > 0 ? i : r-1-i);
                    int py = cy + (dy > 0 ? j : r-1-j);
                    ctx.fill(px, py, px+1, py+1, color);
                }
            }
        }
    }

    private void borderRounded(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
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
                         int color, int startDeg, int endDeg) {
        for (int deg = startDeg; deg <= endDeg; deg += 2) {
            double rad = Math.toRadians(deg);
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
