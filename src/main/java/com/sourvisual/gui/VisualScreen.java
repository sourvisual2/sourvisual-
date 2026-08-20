package com.sourvisual.gui;

import com.sourvisual.config.SourVisualConfig;
import com.sourvisual.hud.Watermark;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

public class VisualScreen extends Screen {

    private static final int SIDE_W   = 108;
    private static final int HEADER_H = 10;
    private static final int R        = 14;
    private static final int RESIZE_GRIP = 10;

    private static final int LOGO_TOP = 10;
    private static final int LOGO_H = 22;
    private static final int TITLE_GAP = 8;
    private static final int SEARCH_GAP = 6;
    private static final int SEARCH_H = 16;
    private static final int TABS_TOP_GAP = 8;

    private Tab currentTab = Tab.VISUAL;

    private int winX, winY;

    private boolean draggingWatermark = false;
    private int dragOffX, dragOffY;

    private boolean draggingMenu = false;
    private int menuDragOffX, menuDragOffY;

    private boolean resizing = false;
    private int resizeStartMouseX, resizeStartMouseY;
    private int resizeStartW, resizeStartH;

    private boolean addBindFormOpen = false;
    private String formName = "";
    private String formCommand = "";
    private int formKeyCode = -1;
    private String formKeyLabel = "";
    private boolean capturingKey = false;
    private int focusedField = 0; // 0 = none, 1 = name, 2 = command

    private String searchQuery = "";
    private boolean searchFocused = false;

    public VisualScreen() {
        super(Text.literal("Sour Visual"));
    }

    @Override
    protected void init() {
        if (SourVisualConfig.menuX == Integer.MIN_VALUE) {
            winX = (this.width - SourVisualConfig.winW) / 2;
            winY = (this.height - SourVisualConfig.winH) / 2;
        } else {
            winX = SourVisualConfig.menuX;
            winY = SourVisualConfig.menuY;
        }
    }

    @Override
    public void removed() {
        super.removed();
        SourVisualConfig.save();
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
        int chip = SourVisualConfig.getChipColor();

        RenderUtils.fillRounded(ctx, winX, winY, winW, winH, R, bg);

        RenderUtils.fillRoundedTop(ctx, winX, winY, winW, HEADER_H, R, header);

        int sideX = winX;
        int sideY = winY + HEADER_H;
        int sideH = winH - HEADER_H;
        RenderUtils.fillRoundedBottomLeft(ctx, sideX, sideY, SIDE_W, sideH, R, side);

        // Логотип "S" — просто крупная буква, без плашки
        int logoY = sideY + LOGO_TOP;
        String logoChar = "S";
        int logoBaseW = this.textRenderer.getWidth(logoChar);
        float logoScale = 2.0f;
        int logoScaledW = Math.round(logoBaseW * logoScale);
        int logoX = sideX + (SIDE_W - logoScaledW) / 2;

        ctx.getMatrices().push();
        ctx.getMatrices().translate(logoX, logoY, 0);
        ctx.getMatrices().scale(logoScale, logoScale, 1f);
        ctx.drawText(this.textRenderer, Text.literal(logoChar), 0, 0, accent, false);
        ctx.getMatrices().pop();

        // Заголовок "sour visual" по центру под логотипом — всегда белый
        int titleY = logoY + LOGO_H + TITLE_GAP;
        String title = "sour visual";
        int titleW = this.textRenderer.getWidth(title);
        ctx.drawText(this.textRenderer, Text.literal(title), sideX + (SIDE_W - titleW) / 2, titleY, text, false);

        // Поисковая строка
        int searchY = titleY + this.textRenderer.fontHeight + SEARCH_GAP;
        int searchX = sideX + 8;
        int searchW = SIDE_W - 16;
        RenderUtils.fillRounded(ctx, searchX, searchY, searchW, SEARCH_H, 4, chip);
        drawMagnifierIcon(ctx, searchX + 5, searchY + 4, textDim);
        String searchDisplay = searchQuery.isEmpty() && !searchFocused
                ? "Search..."
                : (searchFocused ? searchQuery + "_" : searchQuery);
        int searchTextColor = searchQuery.isEmpty() && !searchFocused ? textDim : text;
        ctx.drawText(this.textRenderer, Text.literal(searchDisplay), searchX + 15, searchY + 4, searchTextColor, false);

        // Список вкладок — без иконок, только текст
        int tabsTop = getTabsTop();
        Tab[] tabs = Tab.values();
        int rowH = 24;
        for (int i = 0; i < tabs.length; i++) {
            Tab tab = tabs[i];
            int rowY = tabsTop + i * rowH;
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

    private int getTabsTop() {
        int sideY = winY + HEADER_H;
        int logoY = sideY + LOGO_TOP;
        int titleY = logoY + LOGO_H + TITLE_GAP;
        int searchY = titleY + this.textRenderer.fontHeight + SEARCH_GAP;
        return searchY + SEARCH_H + TABS_TOP_GAP;
    }

    private void drawMagnifierIcon(DrawContext ctx, int x, int y, int color) {
        RenderUtils.fillRounded(ctx, x, y, 6, 6, 3, color);
        ctx.fill(x + 4, y + 4, x + 7, y + 7, color);
    }

    private void drawResizeGrip(DrawContext ctx, int cornerX, int cornerY, int color) {
        for (int i = 0; i < 3; i++) {
            int off = i * 4;
            ctx.fill(cornerX - 3 - off, cornerY - 2, cornerX - 1 - off, cornerY, color);
        }
    }

    private void renderTabContent(DrawContext ctx, int x, int y, int w, int h, int mouseX, int mouseY) {
        switch (currentTab) {
            case VISUAL -> drawVisual(ctx, x, y, w);
            case UTILITIES -> drawUtilities(ctx, x, y, w);
            case KEYBINDS -> {
                if (addBindFormOpen) {
                    drawAddBindForm(ctx, x, y, w);
                } else {
                    drawKeybinds(ctx, x, y, w, h);
                }
            }
            case THEME -> drawThemes(ctx, x, y, w, h);
            case SETTINGS -> drawPlaceholder(ctx, x, y, "More settings coming soon");
        }
    }

    private void drawPlaceholder(DrawContext ctx, int x, int y, String text) {
        ctx.drawText(this.textRenderer, Text.literal(text), x + 10, y + 10, SourVisualConfig.getTextDimColor(), false);
    }

    private static final int ROW_H = 20;
    private static final int ROW_GAP = 6;

    private void drawVisual(DrawContext ctx, int x, int y, int w) {
        int pad = 10;
        int cx = x + pad;
        int cy = y + pad;
        int rowW = w - pad * 2;

        int text = SourVisualConfig.getTextColor();
        int chip = SourVisualConfig.getChipColor();
        int accent = SourVisualConfig.getAccentColor();

        String query = searchQuery.trim().toLowerCase();
        int shown = 0;

        if (query.isEmpty() || "target esp".contains(query) || "targetesp".contains(query)) {
            drawSwitchRow(ctx, cx, cy + shown * (ROW_H + ROW_GAP), rowW, "Target ESP", SourVisualConfig.targetEspEnabled, chip, accent, text);
            shown++;
        }

        if (shown == 0) {
            ctx.drawText(this.textRenderer, Text.literal("No matches"), cx, cy, SourVisualConfig.getTextDimColor(), false);
        }
    }

    private void drawUtilities(DrawContext ctx, int x, int y, int w) {
        int pad = 10;
        int cx = x + pad;
        int cy = y + pad;
        int rowW = w - pad * 2;

        int text = SourVisualConfig.getTextColor();
        int chip = SourVisualConfig.getChipColor();
        int accent = SourVisualConfig.getAccentColor();

        String query = searchQuery.trim().toLowerCase();
        int shown = 0;

        if (query.isEmpty() || "fullbright".contains(query)) {
            drawSwitchRow(ctx, cx, cy + shown * (ROW_H + ROW_GAP), rowW, "Fullbright", SourVisualConfig.fullbrightEnabled, chip, accent, text);
            shown++;
        }
        if (query.isEmpty() || "watermark".contains(query)) {
            drawSwitchRow(ctx, cx, cy + shown * (ROW_H + ROW_GAP), rowW, "Watermark", SourVisualConfig.wmEnabled, chip, accent, text);
            shown++;
        }

        if (shown == 0) {
            ctx.drawText(this.textRenderer, Text.literal("No matches"), cx, cy, SourVisualConfig.getTextDimColor(), false);
        }
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

    // ---------- KEYBINDS ----------

    private static final int ADD_BTN_SIZE = 18;
    private static final int BIND_CARD_H = 42;
    private static final int BIND_CARD_GAP = 6;
    private static final int DELETE_BTN_SIZE = 16;
    private static final int FIELD_H = 16;

    private void drawKeybinds(DrawContext ctx, int x, int y, int w, int h) {
        int pad = 10;
        int cx = x + pad;
        int cy = y + pad;

        int text = SourVisualConfig.getTextColor();
        int textDim = SourVisualConfig.getTextDimColor();
        int chip = SourVisualConfig.getChipColor();
        int accent = SourVisualConfig.getAccentColor();

        int addBtnX = x + w - pad - ADD_BTN_SIZE;
        RenderUtils.fillRounded(ctx, addBtnX, cy, ADD_BTN_SIZE, ADD_BTN_SIZE, 4, accent);
        ctx.drawText(this.textRenderer, Text.literal("+"), addBtnX + ADD_BTN_SIZE / 2 - 2, cy + ADD_BTN_SIZE / 2 - 4, 0xFFFFFFFF, false);

        int listY = cy + ADD_BTN_SIZE + 8;
        int listBottom = y + h - 4;
        ctx.enableScissor(x, listY, x + w, listBottom);

        String query = searchQuery.trim().toLowerCase();
        int shown = 0;
        for (int i = 0; i < SourVisualConfig.keyBinds.size(); i++) {
            SourVisualConfig.KeyBindEntry entry = SourVisualConfig.keyBinds.get(i);
            if (!query.isEmpty()
                    && !entry.name.toLowerCase().contains(query)
                    && !entry.command.toLowerCase().contains(query)) {
                continue;
            }

            int cardY = listY + shown * (BIND_CARD_H + BIND_CARD_GAP);
            int cardW = w - pad * 2;

            RenderUtils.fillRounded(ctx, cx, cardY, cardW, BIND_CARD_H, 6, chip);
            ctx.drawText(this.textRenderer, Text.literal(entry.name), cx + 8, cardY + 6, text, false);
            ctx.drawText(this.textRenderer, Text.literal(entry.keyLabel), cx + 8, cardY + 17, accent, false);
            ctx.drawText(this.textRenderer, Text.literal(entry.command), cx + 8, cardY + 28, textDim, false);

            int delX = cx + cardW - DELETE_BTN_SIZE - 6;
            int delY = cardY + (BIND_CARD_H - DELETE_BTN_SIZE) / 2;
            RenderUtils.fillRounded(ctx, delX, delY, DELETE_BTN_SIZE, DELETE_BTN_SIZE, 4, 0xFFB03A3A);
            ctx.drawText(this.textRenderer, Text.literal("x"), delX + DELETE_BTN_SIZE / 2 - 2, delY + DELETE_BTN_SIZE / 2 - 4, 0xFFFFFFFF, false);

            shown++;
        }

        ctx.disableScissor();

        if (shown == 0) {
            String msg = SourVisualConfig.keyBinds.isEmpty() ? "No binds yet — tap + to add one" : "No matches";
            ctx.drawText(this.textRenderer, Text.literal(msg), cx, listY, textDim, false);
        }
    }

    private void drawAddBindForm(DrawContext ctx, int x, int y, int w) {
        int pad = 10;
        int cx = x + pad;
        int cy = y + pad;
        int fieldW = w - pad * 2;

        int text = SourVisualConfig.getTextColor();
        int textDim = SourVisualConfig.getTextDimColor();
        int chip = SourVisualConfig.getChipColor();
        int accent = SourVisualConfig.getAccentColor();

        ctx.drawText(this.textRenderer, Text.literal("< Add Bind"), cx, cy, text, false);

        int nameLabelY = cy + 16;
        ctx.drawText(this.textRenderer, Text.literal("Name"), cx, nameLabelY, textDim, false);
        int nameFieldY = nameLabelY + 11;
        drawTextField(ctx, cx, nameFieldY, fieldW, formName, focusedField == 1, chip, accent, text);

        int cmdLabelY = nameFieldY + FIELD_H + 8;
        ctx.drawText(this.textRenderer, Text.literal("Command"), cx, cmdLabelY, textDim, false);
        int cmdFieldY = cmdLabelY + 11;
        drawTextField(ctx, cx, cmdFieldY, fieldW, formCommand, focusedField == 2, chip, accent, text);

        int keyLabelY = cmdFieldY + FIELD_H + 8;
        ctx.drawText(this.textRenderer, Text.literal("Key"), cx, keyLabelY, textDim, false);
        int keyFieldY = keyLabelY + 11;
        String keyDisplay = capturingKey ? "Press any key..." : (formKeyLabel.isEmpty() ? "Click to set key" : formKeyLabel);
        int keyColor = capturingKey ? accent : (formKeyLabel.isEmpty() ? textDim : text);
        RenderUtils.fillRounded(ctx, cx, keyFieldY, fieldW, FIELD_H, 4, chip);
        ctx.drawText(this.textRenderer, Text.literal(keyDisplay), cx + 6, keyFieldY + 4, keyColor, false);

        boolean canSave = !formName.trim().isEmpty() && !formCommand.trim().isEmpty() && formKeyCode >= 0;
        int saveY = keyFieldY + FIELD_H + 12;
        int saveColor = canSave ? accent : chip;
        RenderUtils.fillRounded(ctx, cx, saveY, 60, 18, 4, saveColor);
        ctx.drawText(this.textRenderer, Text.literal("Save"), cx + 18, saveY + 5, 0xFFFFFFFF, false);
    }

    private void drawTextField(DrawContext ctx, int x, int y, int w, String value, boolean focused, int chip, int accent, int text) {
        RenderUtils.fillRounded(ctx, x, y, w, FIELD_H, 4, chip);
        String display = focused ? value + "_" : value;
        ctx.drawText(this.textRenderer, Text.literal(display), x + 6, y + 4, text, false);
        if (focused) {
            ctx.fill(x, y, x + 2, y + FIELD_H, accent);
        }
    }

    // ---------- THEME (без фильтрации поиском) ----------

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

        int logoY = sideY + LOGO_TOP;
        int titleY = logoY + LOGO_H + TITLE_GAP;
        int searchY = titleY + this.textRenderer.fontHeight + SEARCH_GAP;
        int searchX = sideX + 8;
        int searchW = SIDE_W - 16;
        if (mouseX >= searchX && mouseX <= searchX + searchW && mouseY >= searchY && mouseY <= searchY + SEARCH_H) {
            searchFocused = true;
            return true;
        }

        int tabsTop = getTabsTop();
        int rowH = 24;
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            int rowY = tabsTop + i * rowH;
            if (mouseX >= sideX && mouseX <= sideX + SIDE_W
                    && mouseY >= rowY && mouseY <= rowY + rowH - 4) {
                currentTab = tabs[i];
                searchFocused = false;
                return true;
            }
        }

        if (currentTab == Tab.VISUAL) {
            if (handleVisualClick((int) mouseX, (int) mouseY)) {
                SourVisualConfig.save();
                return true;
            }
        }

        if (currentTab == Tab.THEME) {
            if (handleThemesClick((int) mouseX, (int) mouseY)) {
                SourVisualConfig.save();
                return true;
            }
        }

        if (currentTab == Tab.UTILITIES) {
            if (handleUtilitiesClick((int) mouseX, (int) mouseY)) {
                SourVisualConfig.save();
                return true;
            }
        }

        if (currentTab == Tab.KEYBINDS) {
            if (addBindFormOpen) {
                if (handleAddBindFormClick((int) mouseX, (int) mouseY)) {
                    return true;
                }
            } else if (handleKeybindsClick((int) mouseX, (int) mouseY)) {
                return true;
            }
        }

        searchFocused = false;

        if (mouseX >= winX && mouseX <= winX + winW && mouseY >= winY && mouseY <= winY + HEADER_H) {
            draggingMenu = true;
            menuDragOffX = (int) mouseX - winX;
            menuDragOffY = (int) mouseY - winY;
            return true;
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
        if (draggingMenu) {
            winX = (int) mouseX - menuDragOffX;
            winY = (int) mouseY - menuDragOffY;
            SourVisualConfig.menuX = winX;
            SourVisualConfig.menuY = winY;
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
        boolean wasActive = resizing || draggingMenu || draggingWatermark;
        resizing = false;
        draggingMenu = false;
        draggingWatermark = false;
        if (wasActive) {
            SourVisualConfig.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (currentTab == Tab.KEYBINDS && addBindFormOpen && focusedField != 0) {
            if (focusedField == 1 && formName.length() < 24) {
                formName += chr;
            } else if (focusedField == 2 && formCommand.length() < 48) {
                formCommand += chr;
            }
            return true;
        }
        if (searchFocused && searchQuery.length() < 32) {
            searchQuery += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (currentTab == Tab.KEYBINDS && addBindFormOpen) {
            if (capturingKey) {
                if (keyCode == GLFW_KEY_ESCAPE) {
                    capturingKey = false;
                    return true;
                }
                formKeyCode = keyCode;
                formKeyLabel = InputUtil.fromKeyCode(keyCode, scanCode).getLocalizedText().getString();
                capturingKey = false;
                return true;
            }
            if (focusedField != 0) {
                if (keyCode == GLFW_KEY_BACKSPACE) {
                    if (focusedField == 1 && !formName.isEmpty()) {
                        formName = formName.substring(0, formName.length() - 1);
                    } else if (focusedField == 2 && !formCommand.isEmpty()) {
                        formCommand = formCommand.substring(0, formCommand.length() - 1);
                    }
                    return true;
                }
                if (keyCode == GLFW_KEY_ESCAPE) {
                    focusedField = 0;
                    return true;
                }
            }
        }

        if (searchFocused) {
            if (keyCode == GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                return true;
            }
            if (keyCode == GLFW_KEY_ESCAPE) {
                searchFocused = false;
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static final int GLFW_KEY_ESCAPE = 256;
    private static final int GLFW_KEY_BACKSPACE = 259;

    private boolean handleVisualClick(int mouseX, int mouseY) {
        int pad = 10;
        int x = winX + SIDE_W;
        int y = winY + HEADER_H;
        int cx = x + pad;
        int cy = y + pad;
        int rowW = (SourVisualConfig.winW - SIDE_W) - pad * 2;

        String query = searchQuery.trim().toLowerCase();
        int shown = 0;

        if (query.isEmpty() || "target esp".contains(query) || "targetesp".contains(query)) {
            if (hitRow(mouseX, mouseY, cx, cy + shown * (ROW_H + ROW_GAP), rowW)) {
                SourVisualConfig.targetEspEnabled = !SourVisualConfig.targetEspEnabled;
                return true;
            }
            shown++;
        }

        return false;
    }

    private boolean handleKeybindsClick(int mouseX, int mouseY) {
        int pad = 10;
        int x = winX + SIDE_W;
        int y = winY + HEADER_H;
        int w = SourVisualConfig.winW - SIDE_W;
        int cx = x + pad;
        int cy = y + pad;

        int addBtnX = x + w - pad - ADD_BTN_SIZE;
        if (mouseX >= addBtnX && mouseX <= addBtnX + ADD_BTN_SIZE && mouseY >= cy && mouseY <= cy + ADD_BTN_SIZE) {
            addBindFormOpen = true;
            formName = "";
            formCommand = "";
            formKeyCode = -1;
            formKeyLabel = "";
            focusedField = 0;
            capturingKey = false;
            return true;
        }

        int listY = cy + ADD_BTN_SIZE + 8;
        int cardW = w - pad * 2;
        String query = searchQuery.trim().toLowerCase();
        int shown = 0;
        for (int i = 0; i < SourVisualConfig.keyBinds.size(); i++) {
            SourVisualConfig.KeyBindEntry entry = SourVisualConfig.keyBinds.get(i);
            if (!query.isEmpty()
                    && !entry.name.toLowerCase().contains(query)
                    && !entry.command.toLowerCase().contains(query)) {
                continue;
            }

            int cardY = listY + shown * (BIND_CARD_H + BIND_CARD_GAP);
            int delX = cx + cardW - DELETE_BTN_SIZE - 6;
            int delY = cardY + (BIND_CARD_H - DELETE_BTN_SIZE) / 2;
            if (mouseX >= delX && mouseX <= delX + DELETE_BTN_SIZE && mouseY >= delY && mouseY <= delY + DELETE_BTN_SIZE) {
                SourVisualConfig.keyBinds.remove(i);
                SourVisualConfig.save();
                return true;
            }

            shown++;
        }

        return false;
    }

    private boolean handleAddBindFormClick(int mouseX, int mouseY) {
        int pad = 10;
        int x = winX + SIDE_W;
        int y = winY + HEADER_H;
        int w = SourVisualConfig.winW - SIDE_W;
        int cx = x + pad;
        int cy = y + pad;
        int fieldW = w - pad * 2;

        if (mouseX >= cx && mouseX <= cx + 70 && mouseY >= cy - 2 && mouseY <= cy + 10) {
            addBindFormOpen = false;
            focusedField = 0;
            capturingKey = false;
            return true;
        }

        int nameLabelY = cy + 16;
        int nameFieldY = nameLabelY + 11;
        if (mouseX >= cx && mouseX <= cx + fieldW && mouseY >= nameFieldY && mouseY <= nameFieldY + FIELD_H) {
            focusedField = 1;
            capturingKey = false;
            return true;
        }

        int cmdLabelY = nameFieldY + FIELD_H + 8;
        int cmdFieldY = cmdLabelY + 11;
        if (mouseX >= cx && mouseX <= cx + fieldW && mouseY >= cmdFieldY && mouseY <= cmdFieldY + FIELD_H) {
            focusedField = 2;
            capturingKey = false;
            return true;
        }

        int keyLabelY = cmdFieldY + FIELD_H + 8;
        int keyFieldY = keyLabelY + 11;
        if (mouseX >= cx && mouseX <= cx + fieldW && mouseY >= keyFieldY && mouseY <= keyFieldY + FIELD_H) {
            focusedField = 0;
            capturingKey = true;
            return true;
        }

        boolean canSave = !formName.trim().isEmpty() && !formCommand.trim().isEmpty() && formKeyCode >= 0;
        int saveY = keyFieldY + FIELD_H + 12;
        if (canSave && mouseX >= cx && mouseX <= cx + 60 && mouseY >= saveY && mouseY <= saveY + 18) {
            SourVisualConfig.keyBinds.add(new SourVisualConfig.KeyBindEntry(
                    formName.trim(), formCommand.trim(), formKeyCode, formKeyLabel));
            SourVisualConfig.save();
            addBindFormOpen = false;
            focusedField = 0;
            capturingKey = false;
            return true;
        }

        focusedField = 0;
        return true;
    }

    private boolean handleUtilitiesClick(int mouseX, int mouseY) {
        int pad = 10;
        int x = winX + SIDE_W;
        int y = winY + HEADER_H;
        int cx = x + pad;
        int cy = y + pad;
        int rowW = (SourVisualConfig.winW - SIDE_W) - pad * 2;

        String query = searchQuery.trim().toLowerCase();
        int shown = 0;

        if (query.isEmpty() || "fullbright".contains(query)) {
            if (hitRow(mouseX, mouseY, cx, cy + shown * (ROW_H + ROW_GAP), rowW)) {
                SourVisualConfig.fullbrightEnabled = !SourVisualConfig.fullbrightEnabled;
                return true;
            }
            shown++;
        }
        if (query.isEmpty() || "watermark".contains(query)) {
            if (hitRow(mouseX, mouseY, cx, cy + shown * (ROW_H + ROW_GAP), rowW)) {
                SourVisualConfig.wmEnabled = !SourVisualConfig.wmEnabled;
                return true;
            }
            shown++;
        }

        return false;
    }

    private boolean hitRow(int mouseX, int mouseY, int x, int y, int w) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + ROW_H;
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

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !addBindFormOpen && !capturingKey && !searchFocused;
    }
}
