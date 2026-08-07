package com.sourvisual.client.gui;

import com.sourvisual.client.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class VisualMenuScreen extends Screen {

    // Размеры главного окна
    private static final int WIN_W    = 315;
    private static final int WIN_H    = 195;
    private static final int SIDE_W   = 82;
    private static final int HEADER_H = 27;
    private static final int R        = 6;

    // Цвета UI
    private static final int C_BG      = 0xF00D0D14;
    private static final int C_BORDER  = 0xFF2A2A38;
    private static final int C_DIVIDER = 0xFF1A1A26;
    private static final int C_SEL_BG  = 0x33FFFFFF;
    private static final int C_WHITE   = 0xFFFFFFFF;
    private static final int C_DIM     = 0xFF888899;
    private static final int C_ACCENT1 = 0xFF9B6FFF;
    private static final int C_ACCENT2 = 0xFFCC99FF;
    private static final int C_HOVER   = 0xFFCCCCDD;

    // Размеры палитры
    private static final int PW     = 180;
    private static final int PH     = 160;
    private static final int SV_SZ  = 110;
    private static final int HUE_W  = 14;
    private static final int GAP    = 8;

    private int wX, wY;
    private TextFieldWidget searchField;
    private MenuCategory selected = MenuCategory.VISUAL;

    private boolean colorPickerOpen = false;
    private float hue = 0.75f, sat = 0.5f, bri = 0.8f;
    private boolean dragSV = false, dragHue = false;

    public VisualMenuScreen() {
        super(Text.literal("Sour Visual"));
        float[] hsv = rgbToHsv(ModConfig.hitColorR, ModConfig.hitColorG, ModConfig.hitColorB);
        hue = hsv[0]; sat = hsv[1]; bri = hsv[2];
    }

    @Override
    protected void init() {
        wX = (width  - WIN_W) / 2;
        wY = (height - WIN_H) / 2;

        searchField = new TextFieldWidget(textRenderer,
                wX + SIDE_W + 8, wY + 8,
                WIN_W - SIDE_W - 28, 11, Text.literal(""));
        searchField.setPlaceholder(Text.literal("Search..."));
        searchField.setMaxLength(64);
        searchField.setDrawsBackground(false);
        addSelectableChild(searchField);
    }

    // ── Render ───────────────────────────────────────────────────────────────
    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);

        // главное окно
        fillR(ctx, wX, wY, WIN_W, WIN_H, R, C_BG);
        borderR(ctx, wX, wY, WIN_W, WIN_H, R, C_BORDER);
        ctx.drawVerticalLine(wX + SIDE_W, wY, wY + WIN_H, C_BORDER);

        // заголовок
        int p1w = textRenderer.getWidth("Sour ");
        ctx.drawTextWithShadow(textRenderer, Text.literal("Sour "),  wX + 8, wY + 9, C_ACCENT1);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Visual"), wX + 8 + p1w, wY + 9, C_ACCENT2);

        ctx.drawHorizontalLine(wX, wX + WIN_W, wY + HEADER_H, C_DIVIDER);
        ctx.drawVerticalLine(wX + SIDE_W, wY, wY + HEADER_H, C_BORDER);

        // поиск
        ctx.fill(wX + SIDE_W + 5, wY + 6, wX + WIN_W - 5, wY + 19, 0x22FFFFFF);
        searchField.render(ctx, mx, my, delta);
        ctx.drawTextWithShadow(textRenderer, Text.literal("⌕"), wX + WIN_W - 12, wY + 9, C_DIM);

        // категории
        int catY = wY + HEADER_H + 7;
        for (MenuCategory cat : MenuCategory.values()) {
            boolean hov = mx >= wX+3 && mx <= wX+SIDE_W-3 && my >= catY && my <= catY+18;
            boolean sel = cat == selected;
            if (sel) ctx.fill(wX+3, catY, wX+SIDE_W-3, catY+18, C_SEL_BG);
            int col = sel ? C_WHITE : (hov ? C_HOVER : C_DIM);
            ctx.drawTextWithShadow(textRenderer,
                    Text.literal(icon(cat) + " " + cat.label), wX+7, catY+5, col);
            catY += 22;
        }

        // контент (рисуем ДО палитры)
        renderContent(ctx, wX + SIDE_W + 8, wY + HEADER_H + 8, mx, my);

        // палитра поверх всего
        if (colorPickerOpen) renderPicker(ctx, mx, my);
    }

    // ── Панели ───────────────────────────────────────────────────────────────
    private void renderContent(DrawContext ctx, int x, int y, int mx, int my) {
        switch (selected) {
            case VISUAL   -> renderVisual(ctx, x, y);
            case EFFECTS  -> ctx.drawTextWithShadow(textRenderer, Text.literal("— empty —"), x, y, C_DIM);
            case SETTINGS -> ctx.drawTextWithShadow(textRenderer, Text.literal("— empty —"), x, y, C_DIM);
        }
    }

    private void renderVisual(DrawContext ctx, int x, int y) {
        int rowW = WIN_W - SIDE_W - 12;
        int rowH = 34;

        // фон строки
        fillR(ctx, x-2, y, rowW+2, rowH, 3, 0x22FFFFFF);

        // название
        ctx.drawTextWithShadow(textRenderer, Text.literal("Hit Color"), x+3, y+6, C_WHITE);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Цвет скина при ударе."), x+3, y+18, C_DIM);

        // круглый превью цвета
        int circD = 14;
        int circX = x + rowW - 54;
        int circY = y + rowH/2 - circD/2;
        int pc = 0xFF000000 | (ModConfig.hitColorR << 16) | (ModConfig.hitColorG << 8) | ModConfig.hitColorB;
        circle(ctx, circX, circY, circD, pc);

        // шестерёнка
        ctx.drawTextWithShadow(textRenderer, Text.literal("⚙"), circX + circD + 2, circY + 2, C_DIM);

        // овальный переключатель
        boolean on = ModConfig.hitColorEnabled;
        int sw = 26, sh = 12;
        int sx = x + rowW - sw - 2;
        int sy = y + rowH/2 - sh/2;
        fillR(ctx, sx, sy, sw, sh, sh/2, on ? 0xFF4A3A9F : 0xFF2A2A35);
        borderR(ctx, sx, sy, sw, sh, sh/2, C_BORDER);
        int kd = sh - 4;
        int kx = on ? sx + sw - kd - 2 : sx + 2;
        circle(ctx, kx, sy+2, kd, on ? C_WHITE : 0xFF555566);
    }

    // ── HSV Палитра ──────────────────────────────────────────────────────────
    private int pX() { return wX + (WIN_W - PW) / 2; }
    private int pY() { return wY + (WIN_H - PH) / 2; }
    private int svX(){ return pX() + 10; }
    private int svY(){ return pY() + 30; }
    private int hX() { return svX() + SV_SZ + GAP; }

    private void renderPicker(DrawContext ctx, int mx, int my) {
        int px = pX(), py = pY();

        // фон палитры — поверх меню
        fillR(ctx, px, py, PW, PH, R, 0xF5141420);
        borderR(ctx, px, py, PW, PH, R, C_ACCENT1);

        ctx.drawTextWithShadow(textRenderer, Text.literal("Цвет удара"), px+10, py+10, C_WHITE);
        ctx.drawTextWithShadow(textRenderer, Text.literal("✕"), px+PW-14, py+10, C_DIM);

        int sx = svX(), sy = svY();

        // SV квадрат
        for (int i = 0; i < SV_SZ; i++) {
            for (int j = 0; j < SV_SZ; j++) {
                float s = i / (float)(SV_SZ - 1);
                float v = 1f - j / (float)(SV_SZ - 1);
                int[] rgb = hsv(hue, s, v);
                ctx.fill(sx+i, sy+j, sx+i+1, sy+j+1,
                        0xFF000000|(rgb[0]<<16)|(rgb[1]<<8)|rgb[2]);
            }
        }
        borderR(ctx, sx, sy, SV_SZ, SV_SZ, 3, 0xFF333344);

        // курсор SV
        int csx = sx + (int)(sat * (SV_SZ-1));
        int csy = sy + (int)((1f - bri) * (SV_SZ-1));
        ctx.fill(csx-4, csy-1, csx+5, csy+2, C_WHITE);
        ctx.fill(csx-1, csy-4, csx+2, csy+5, C_WHITE);

        // Hue полоса
        int hx = hX(), hy = sy;
        for (int j = 0; j < SV_SZ; j++) {
            float h = j / (float)(SV_SZ-1);
            int[] rgb = hsv(h, 1f, 1f);
            ctx.fill(hx, hy+j, hx+HUE_W, hy+j+1,
                    0xFF000000|(rgb[0]<<16)|(rgb[1]<<8)|rgb[2]);
        }
        borderR(ctx, hx, hy, HUE_W, SV_SZ, 3, 0xFF333344);

        // курсор Hue
        int hcy = hy + (int)(hue * (SV_SZ-1));
        ctx.fill(hx-2, hcy-1, hx+HUE_W+2, hcy+2, C_WHITE);

        // превью цвета — круг
        int prevD = 18;
        int prevX = hx + HUE_W + GAP;
        int prevY = hy + SV_SZ/2 - prevD/2;
        int pc = 0xFF000000|(ModConfig.hitColorR<<16)|(ModConfig.hitColorG<<8)|ModConfig.hitColorB;
        circle(ctx, prevX, prevY, prevD, pc);
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (colorPickerOpen) {
            int px = pX(), py = pY();
            int sx = svX(), sy = svY(), hx = hX();

            // закрыть
            if (mx >= px+PW-18 && mx <= px+PW-4 && my >= py+4 && my <= py+20) {
                colorPickerOpen = false; ModConfig.save(); return true;
            }
            // SV
            if (mx >= sx && mx <= sx+SV_SZ && my >= sy && my <= sy+SV_SZ) {
                dragSV = true; moveSV((int)mx,(int)my); return true;
            }
            // Hue
            if (mx >= hx && mx <= hx+HUE_W && my >= sy && my <= sy+SV_SZ) {
                dragHue = true; moveHue((int)my); return true;
            }
            return true; // поглощаем клики внутри пикера
        }

        // категории
        int catY = wY + HEADER_H + 7;
        for (MenuCategory cat : MenuCategory.values()) {
            if (mx >= wX+3 && mx <= wX+SIDE_W-3 && my >= catY && my <= catY+18) {
                selected = cat; return true;
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
            int circY = y + rowH/2 - circD/2;

            // шестерёнка
            if (mx >= circX+circD+2 && mx <= circX+circD+12 && my >= circY && my <= circY+circD) {
                colorPickerOpen = true;
                float[] h = rgbToHsv(ModConfig.hitColorR, ModConfig.hitColorG, ModConfig.hitColorB);
                hue=h[0]; sat=h[1]; bri=h[2];
                return true;
            }

            // переключатель
            int sw=26, sh=12;
            int switchX = x+rowW-sw-2;
            int switchY = y+rowH/2-sh/2;
            if (mx >= switchX && mx <= switchX+sw && my >= switchY && my <= switchY+sh) {
                ModConfig.hitColorEnabled = !ModConfig.hitColorEnabled;
                ModConfig.save(); return true;
            }
        }

        if (searchField.isMouseOver(mx, my)) searchField.setFocused(true);
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragSV)  { moveSV((int)mx,(int)my); return true; }
        if (dragHue) { moveHue((int)my);        return true; }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (dragSV || dragHue) { dragSV=false; dragHue=false; ModConfig.save(); }
        return super.mouseReleased(mx, my, button);
    }

    private void moveSV(int mx, int my) {
        int sx=svX(), sy=svY();
        sat = clamp((mx-sx)/(float)(SV_SZ-1));
        bri = clamp(1f-(my-sy)/(float)(SV_SZ-1));
        int[] rgb = hsv(hue, sat, bri);
        ModConfig.hitColorR=rgb[0]; ModConfig.hitColorG=rgb[1]; ModConfig.hitColorB=rgb[2];
    }

    private void moveHue(int my) {
        hue = clamp((my-svY())/(float)(SV_SZ-1));
        int[] rgb = hsv(hue, sat, bri);
        ModConfig.hitColorR=rgb[0]; ModConfig.hitColorG=rgb[1]; ModConfig.hitColorB=rgb[2];
    }

    private float clamp(float v) { return Math.max(0, Math.min(1, v)); }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC или Right Shift закрывают
        if (keyCode == 256 || keyCode == 344) {
            if (colorPickerOpen) { colorPickerOpen = false; ModConfig.save(); }
            else this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }

    // ── Рисование ────────────────────────────────────────────────────────────
    private void circle(DrawContext ctx, int x, int y, int d, int color) {
        float r = d/2f, cx=x+r-0.5f, cy=y+r-0.5f;
        for (int i=x; i<x+d; i++)
            for (int j=y; j<y+d; j++)
                if (Math.sqrt((i-cx)*(i-cx)+(j-cy)*(j-cy)) <= r)
                    ctx.fill(i,j,i+1,j+1,color);
    }

    private void fillR(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        int rr = Math.min(r, Math.min(w/2, h/2));
        ctx.fill(x+rr,y,   x+w-rr,y+h,   color);
        ctx.fill(x,   y+rr,x+rr,  y+h-rr,color);
        ctx.fill(x+w-rr,y+rr,x+w,y+h-rr, color);
        corner(ctx,x,     y,     rr, 1, 1,color);
        corner(ctx,x+w-rr,y,     rr,-1, 1,color);
        corner(ctx,x,     y+h-rr,rr, 1,-1,color);
        corner(ctx,x+w-rr,y+h-rr,rr,-1,-1,color);
    }

    private void corner(DrawContext ctx, int cx, int cy, int r,
                        int dx, int dy, int color) {
        for (int i=0;i<r;i++) for (int j=0;j<r;j++)
            if (Math.sqrt((r-1-i)*(r-1-i)+(r-1-j)*(r-1-j)) < r-0.5) {
                int px=cx+(dx>0?i:r-1-i), py=cy+(dy>0?j:r-1-j);
                ctx.fill(px,py,px+1,py+1,color);
            }
    }

    private void borderR(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        int rr=Math.min(r,Math.min(w/2,h/2));
        ctx.drawHorizontalLine(x+rr,x+w-rr,y,  color);
        ctx.drawHorizontalLine(x+rr,x+w-rr,y+h,color);
        ctx.drawVerticalLine(x,  y+rr,y+h-rr,color);
        ctx.drawVerticalLine(x+w,y+rr,y+h-rr,color);
        arc(ctx,x+rr,  y+rr,  rr,color,180,270);
        arc(ctx,x+w-rr,y+rr,  rr,color,270,360);
        arc(ctx,x+rr,  y+h-rr,rr,color, 90,180);
        arc(ctx,x+w-rr,y+h-rr,rr,color,  0, 90);
    }

    private void arc(DrawContext ctx, int cx, int cy, int r,
                     int color, int s, int e) {
        for (int d=s;d<=e;d+=2) {
            double rad=Math.toRadians(d);
            int px=cx+(int)Math.round((r-1)*Math.cos(rad));
            int py=cy-(int)Math.round((r-1)*Math.sin(rad));
            ctx.fill(px,py,px+1,py+1,color);
        }
    }

    // ── HSV ──────────────────────────────────────────────────────────────────
    private static int[] hsv(float h, float s, float v) {
        float r,g,b;
        if (s==0){r=g=b=v;}
        else {
            int i=(int)(h*6); float f=h*6-i;
            float p=v*(1-s),q=v*(1-f*s),t=v*(1-(1-f)*s);
            switch(i%6){
                case 0->{r=v;g=t;b=p;} case 1->{r=q;g=v;b=p;}
                case 2->{r=p;g=v;b=t;} case 3->{r=p;g=q;b=v;}
                case 4->{r=t;g=p;b=v;} default->{r=v;g=p;b=q;}
            }
        }
        return new int[]{(int)(r*255),(int)(g*255),(int)(b*255)};
    }

    private static float[] rgbToHsv(int r, int g, int b) {
        float rf=r/255f,gf=g/255f,bf=b/255f;
        float max=Math.max(rf,Math.max(gf,bf)),min=Math.min(rf,Math.min(gf,bf));
        float d=max-min,h=0,s=max==0?0:d/max,v=max;
        if(d!=0){
            if(max==rf)      h=((gf-bf)/d%6)/6f;
            else if(max==gf) h=((bf-rf)/d+2)/6f;
            else             h=((rf-gf)/d+4)/6f;
            if(h<0) h+=1;
        }
        return new float[]{h,s,v};
    }

    private String icon(MenuCategory cat) {
        return switch(cat){
            case EFFECTS->"★"; case VISUAL->"◉"; case SETTINGS->"⚙";
        };
    }
}
