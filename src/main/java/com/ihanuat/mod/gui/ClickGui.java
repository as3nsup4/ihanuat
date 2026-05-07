package com.ihanuat.mod.gui;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import com.ihanuat.mod.MacroConfig;
import com.ihanuat.mod.MacroStateManager;
import com.ihanuat.mod.I18n;
import com.ihanuat.mod.modules.profitTracker.ProfitManager;

import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClickGui extends Screen {

    private static String tr(String english, String simplifiedChinese) {
        return I18n.tr(english, simplifiedChinese);
    }

    static int C_BG() {
        return MacroConfig.themePanelBg;
    }

    static int C_HDR() {
        return MacroConfig.themePanelHeader;
    }

    static int C_LINE() {
        return MacroConfig.themeAccent;
    }

    static int C_TXT() {
        return MacroConfig.themeText;
    }

    static int C_DIM() {
        return MacroConfig.themeTextDim;
    }

    static int C_ON() {
        return MacroConfig.themeToggleOn;
    }

    static int C_ON2() {
        return brighten(MacroConfig.themeToggleOn, 0x222222);
    }

    static int C_OFF() {
        return MacroConfig.themeToggleOff;
    }

    static int C_HOVER() {
        return darken(MacroConfig.themePanelHeader, 0x060606);
    }

    static int C_SBGR() {
        return darken(MacroConfig.themePanelBg, 0x050505);
    }

    static int C_SFILL() {
        return MacroConfig.themeSliderFill;
    }

    static int C_SKNOB() {
        return brighten(MacroConfig.themeSliderFill, 0x333333);
    }

    static int C_SPBG() {
        return MacroConfig.themePanelBg;
    }

    static int C_SPBD() {
        return MacroConfig.themeAccent;
    }

    static int C_ACC() {
        return MacroConfig.themeAccent;
    }

    static int C_BTN() {
        return MacroConfig.themeButtonHover;
    }

    static int brighten(int c, int a) {
        return (c & 0xFF000000)
                | (Math.min(255, ((c >> 16) & 0xFF) + ((a >> 16) & 0xFF)) << 16)
                | (Math.min(255, ((c >> 8) & 0xFF) + ((a >> 8) & 0xFF)) << 8)
                | Math.min(255, (c & 0xFF) + (a & 0xFF));
    }

    static int darken(int c, int a) {
        return (c & 0xFF000000)
                | (Math.max(0, ((c >> 16) & 0xFF) - ((a >> 16) & 0xFF)) << 16)
                | (Math.max(0, ((c >> 8) & 0xFF) - ((a >> 8) & 0xFF)) << 8)
                | Math.max(0, (c & 0xFF) - (a & 0xFF));
    }

    static Character fallbackCharFromKey(int key, int scan, int mods) {
        if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) return null;
        if ((mods & GLFW.GLFW_MOD_SHIFT) != 0 && key >= 48 && key <= 57) return (char) key;
        String name = GLFW.glfwGetKeyName(key, scan);
        if (name == null || name.length() != 1) return null;
        char c = name.charAt(0);
        if ((mods & GLFW.GLFW_MOD_SHIFT) == 0) return c;
        if (Character.isLetter(c)) return Character.toUpperCase(c);
        return switch (c) {
            case '`' -> '~';
            case '1' -> '!';
            case '2' -> '@';
            case '3' -> '#';
            case '4' -> '$';
            case '5' -> '%';
            case '6' -> '^';
            case '7' -> '&';
            case '8' -> '*';
            case '9' -> '(';
            case '0' -> ')';
            case '-' -> '_';
            case '=' -> '+';
            case '[' -> '{';
            case ']' -> '}';
            case '\\' -> '|';
            case ';' -> ':';
            case '\'' -> '"';
            case ',' -> '<';
            case '.' -> '>';
            case '/' -> '?';
            default -> c;
        };
    }

    private static String encodeDefaultTheme() {
        int[] colors = {
                0xF0101018, 0xFF18182C, 0xFF5050A0, 0xFFCCCCCC, 0xFF666677,
                0xFF4444BB, 0xFF2A2A3A, 0xFF3A3A99, 0xFF4444BB,
                MacroConfig.DEFAULT_HUD_BG_COLOR, MacroConfig.DEFAULT_HUD_ACCENT_COLOR,
                MacroConfig.DEFAULT_HUD_TITLE_COLOR, MacroConfig.DEFAULT_HUD_LABEL_COLOR,
                MacroConfig.DEFAULT_HUD_VALUE_COLOR, MacroConfig.DEFAULT_HUD_BAR_BG_COLOR,
                MacroConfig.DEFAULT_HUD_BAR_FILL_COLOR, MacroConfig.DEFAULT_HUD_STATE_OFF_COLOR,
                MacroConfig.DEFAULT_HUD_STATE_FARMING_COLOR, MacroConfig.DEFAULT_HUD_STATE_CLEANING_COLOR,
                MacroConfig.DEFAULT_HUD_STATE_RECOVERING_COLOR, MacroConfig.DEFAULT_HUD_STATE_VISITING_COLOR,
                MacroConfig.DEFAULT_HUD_STATE_AUTOSELLING_COLOR, MacroConfig.DEFAULT_HUD_STATE_SPRAYING_COLOR
        };
        StringBuilder sb = new StringBuilder("v4:");
        for (int i = 0; i < colors.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(String.format("%08X", colors[i]));
        }
        sb.append(",NONE,1,180");
        return java.util.Base64.getEncoder().encodeToString(sb.toString().getBytes());
    }

    static String encodeTheme() {
        String s = String.format("v4:%08X,%08X,%08X,%08X,%08X,%08X,%08X,%08X,%08X,%08X,%08X,%08X,%08X,%08X,%08X,%08X,%08X,%08X,%08X,%08X,%08X,%08X,%08X,%s,%d,%d",
                MacroConfig.themePanelBg, MacroConfig.themePanelHeader, MacroConfig.themeAccent,
                MacroConfig.themeText, MacroConfig.themeTextDim, MacroConfig.themeToggleOn,
                MacroConfig.themeToggleOff, MacroConfig.themeSliderFill, MacroConfig.themeButtonHover,
                MacroConfig.hudBgColor, MacroConfig.hudAccentColor, MacroConfig.hudTitleColor,
                MacroConfig.hudLabelColor, MacroConfig.hudValueColor, MacroConfig.hudBarBgColor,
                MacroConfig.hudBarFillColor, MacroConfig.hudStateOffColor, MacroConfig.hudStateFarmingColor,
                MacroConfig.hudStateCleaningColor, MacroConfig.hudStateRecoveringColor,
                MacroConfig.hudStateVisitingColor, MacroConfig.hudStateAutosellingColor,
                MacroConfig.hudStateSprayingColor, MacroConfig.themeTextStyle.name(),
                MacroConfig.themeOutlineSize, MacroConfig.themeShadowOpacity);
        return Base64.getEncoder().encodeToString(s.getBytes());
    }

    static boolean applyThemeCode(String code) {
        try {
            String s = new String(Base64.getDecoder().decode(code.trim()));
            if (s.startsWith("v4:")) {
                String[] parts = s.substring(3).split(",");
                if (parts.length != 26) return false;
                MacroConfig.themePanelBg            = (int) Long.parseLong(parts[0], 16);
                MacroConfig.themePanelHeader        = (int) Long.parseLong(parts[1], 16);
                MacroConfig.themeAccent             = (int) Long.parseLong(parts[2], 16);
                MacroConfig.themeText               = (int) Long.parseLong(parts[3], 16);
                MacroConfig.themeTextDim            = (int) Long.parseLong(parts[4], 16);
                MacroConfig.themeToggleOn           = (int) Long.parseLong(parts[5], 16);
                MacroConfig.themeToggleOff          = (int) Long.parseLong(parts[6], 16);
                MacroConfig.themeSliderFill         = (int) Long.parseLong(parts[7], 16);
                MacroConfig.themeButtonHover        = (int) Long.parseLong(parts[8], 16);
                MacroConfig.hudBgColor              = (int) Long.parseLong(parts[9], 16);
                MacroConfig.hudAccentColor          = (int) Long.parseLong(parts[10], 16);
                MacroConfig.hudTitleColor           = (int) Long.parseLong(parts[11], 16);
                MacroConfig.hudLabelColor           = (int) Long.parseLong(parts[12], 16);
                MacroConfig.hudValueColor           = (int) Long.parseLong(parts[13], 16);
                MacroConfig.hudBarBgColor           = (int) Long.parseLong(parts[14], 16);
                MacroConfig.hudBarFillColor         = (int) Long.parseLong(parts[15], 16);
                MacroConfig.hudStateOffColor        = (int) Long.parseLong(parts[16], 16);
                MacroConfig.hudStateFarmingColor    = (int) Long.parseLong(parts[17], 16);
                MacroConfig.hudStateCleaningColor   = (int) Long.parseLong(parts[18], 16);
                MacroConfig.hudStateRecoveringColor = (int) Long.parseLong(parts[19], 16);
                MacroConfig.hudStateVisitingColor   = (int) Long.parseLong(parts[20], 16);
                MacroConfig.hudStateAutosellingColor = (int) Long.parseLong(parts[21], 16);
                MacroConfig.hudStateSprayingColor   = (int) Long.parseLong(parts[22], 16);
                try { MacroConfig.themeTextStyle = MacroConfig.TextStyle.valueOf(parts[23]); }
                catch (IllegalArgumentException ignored) { MacroConfig.themeTextStyle = MacroConfig.TextStyle.NONE; }
                try { MacroConfig.themeOutlineSize   = Math.max(1, Math.min(3, Integer.parseInt(parts[24].trim()))); }
                catch (NumberFormatException ignored) { MacroConfig.themeOutlineSize = 1; }
                try { MacroConfig.themeShadowOpacity = Math.max(0, Math.min(255, Integer.parseInt(parts[25].trim()))); }
                catch (NumberFormatException ignored) { MacroConfig.themeShadowOpacity = 180; }
            } else if (s.startsWith("v3:")) {
                String[] parts = s.substring(3).split(",");
                if (parts.length != 24) return false;
                MacroConfig.themePanelBg            = (int) Long.parseLong(parts[0], 16);
                MacroConfig.themePanelHeader        = (int) Long.parseLong(parts[1], 16);
                MacroConfig.themeAccent             = (int) Long.parseLong(parts[2], 16);
                MacroConfig.themeText               = (int) Long.parseLong(parts[3], 16);
                MacroConfig.themeTextDim            = (int) Long.parseLong(parts[4], 16);
                MacroConfig.themeToggleOn           = (int) Long.parseLong(parts[5], 16);
                MacroConfig.themeToggleOff          = (int) Long.parseLong(parts[6], 16);
                MacroConfig.themeSliderFill         = (int) Long.parseLong(parts[7], 16);
                MacroConfig.themeButtonHover        = (int) Long.parseLong(parts[8], 16);
                MacroConfig.hudBgColor              = (int) Long.parseLong(parts[9], 16);
                MacroConfig.hudAccentColor          = (int) Long.parseLong(parts[10], 16);
                MacroConfig.hudTitleColor           = (int) Long.parseLong(parts[11], 16);
                MacroConfig.hudLabelColor           = (int) Long.parseLong(parts[12], 16);
                MacroConfig.hudValueColor           = (int) Long.parseLong(parts[13], 16);
                MacroConfig.hudBarBgColor           = (int) Long.parseLong(parts[14], 16);
                MacroConfig.hudBarFillColor         = (int) Long.parseLong(parts[15], 16);
                MacroConfig.hudStateOffColor        = (int) Long.parseLong(parts[16], 16);
                MacroConfig.hudStateFarmingColor    = (int) Long.parseLong(parts[17], 16);
                MacroConfig.hudStateCleaningColor   = (int) Long.parseLong(parts[18], 16);
                MacroConfig.hudStateRecoveringColor = (int) Long.parseLong(parts[19], 16);
                MacroConfig.hudStateVisitingColor   = (int) Long.parseLong(parts[20], 16);
                MacroConfig.hudStateAutosellingColor = (int) Long.parseLong(parts[21], 16);
                MacroConfig.hudStateSprayingColor   = (int) Long.parseLong(parts[22], 16);
                try { MacroConfig.themeTextStyle = MacroConfig.TextStyle.valueOf(parts[23]); }
                catch (IllegalArgumentException ignored) { MacroConfig.themeTextStyle = MacroConfig.TextStyle.NONE; }
            } else if (s.startsWith("v2:")) {
                String[] parts = s.substring(3).split(",");
                if (parts.length != 23) return false;
                MacroConfig.themePanelBg            = (int) Long.parseLong(parts[0], 16);
                MacroConfig.themePanelHeader        = (int) Long.parseLong(parts[1], 16);
                MacroConfig.themeAccent             = (int) Long.parseLong(parts[2], 16);
                MacroConfig.themeText               = (int) Long.parseLong(parts[3], 16);
                MacroConfig.themeTextDim            = (int) Long.parseLong(parts[4], 16);
                MacroConfig.themeToggleOn           = (int) Long.parseLong(parts[5], 16);
                MacroConfig.themeToggleOff          = (int) Long.parseLong(parts[6], 16);
                MacroConfig.themeSliderFill         = (int) Long.parseLong(parts[7], 16);
                MacroConfig.themeButtonHover        = (int) Long.parseLong(parts[8], 16);
                MacroConfig.hudBgColor              = (int) Long.parseLong(parts[9], 16);
                MacroConfig.hudAccentColor          = (int) Long.parseLong(parts[10], 16);
                MacroConfig.hudTitleColor           = (int) Long.parseLong(parts[11], 16);
                MacroConfig.hudLabelColor           = (int) Long.parseLong(parts[12], 16);
                MacroConfig.hudValueColor           = (int) Long.parseLong(parts[13], 16);
                MacroConfig.hudBarBgColor           = (int) Long.parseLong(parts[14], 16);
                MacroConfig.hudBarFillColor         = (int) Long.parseLong(parts[15], 16);
                MacroConfig.hudStateOffColor        = (int) Long.parseLong(parts[16], 16);
                MacroConfig.hudStateFarmingColor    = (int) Long.parseLong(parts[17], 16);
                MacroConfig.hudStateCleaningColor   = (int) Long.parseLong(parts[18], 16);
                MacroConfig.hudStateRecoveringColor = (int) Long.parseLong(parts[19], 16);
                MacroConfig.hudStateVisitingColor   = (int) Long.parseLong(parts[20], 16);
                MacroConfig.hudStateAutosellingColor = (int) Long.parseLong(parts[21], 16);
                MacroConfig.hudStateSprayingColor   = (int) Long.parseLong(parts[22], 16);
                MacroConfig.themeTextStyle = MacroConfig.TextStyle.NONE;
            } else {
                String[] parts = s.split(",");
                if (parts.length != 9) return false;
                MacroConfig.themePanelBg     = (int) Long.parseLong(parts[0], 16);
                MacroConfig.themePanelHeader = (int) Long.parseLong(parts[1], 16);
                MacroConfig.themeAccent      = (int) Long.parseLong(parts[2], 16);
                MacroConfig.themeText        = (int) Long.parseLong(parts[3], 16);
                MacroConfig.themeTextDim     = (int) Long.parseLong(parts[4], 16);
                MacroConfig.themeToggleOn    = (int) Long.parseLong(parts[5], 16);
                MacroConfig.themeToggleOff   = (int) Long.parseLong(parts[6], 16);
                MacroConfig.themeSliderFill  = (int) Long.parseLong(parts[7], 16);
                MacroConfig.themeButtonHover = (int) Long.parseLong(parts[8], 16);
                MacroConfig.themeTextStyle = MacroConfig.TextStyle.NONE;
                MacroConfig.themeOutlineSize = 1;
                MacroConfig.themeShadowOpacity = 180;
            }
            MacroConfig.save();
            return true;
        } catch (Exception e) { return false; }
    }

    private static final int PANEL_W = 180;
    private static final int HEADER_H = 18;
    private static final int ENTRY_H = 18;
    private static final int ENTRY_PAD = 3;
    private static final int PANEL_RADIUS = 4;
    private static final int SEARCH_H = 16;

    private boolean shiftHeld = false;
    private String searchQuery = "";
    private boolean searchActive = false;

    /** Which topic the helper is currently showing. null = no topic. */
    private static String helperTopic = null;
    /** Human-readable title shown in the helper header, e.g. "Chat Rules". */
    private static String helperTitle = null;
    /** Position of the helper panel (persisted across openings via static). */
    private static int helperX = -1;
    private static int helperY = -1;
    private static boolean helperDragging = false;
    private static int helperDragOffX, helperDragOffY;
    private static int helperScrollOffset = 0;
    private static final int HELPER_W = 270;
    private static final int HELPER_MAX_CONTENT_H = 168;
    private static int activeLibraryIndex = -1;
    private static String previewOriginalCode = null;

    private static void revertLibraryPreview() {
        if (previewOriginalCode != null) {
            applyThemeCode(previewOriginalCode);
            previewOriginalCode = null;
            activeLibraryIndex = -1;
        }
    }

    private final List<Panel> panels = new ArrayList<>();
    private SubPanel activeSubPanel = null;
    private Panel draggingPanel = null;
    private int dragOffX, dragOffY;
    private boolean dragMovedPanel = false;
    private SliderEntry draggingSlider = null;
    private Panel draggingSliderPanel = null;
    private Panel scrollbarPanel = null;
    private int scrollbarDragStartY, scrollbarDragStartOffset;

    public ClickGui() {
        super(Component.literal("ihanuat"));
    }

    @Override
    protected void init() {
        panels.clear();
        buildPanels();
        ScreenMouseEvents.allowMouseClick(this).register((scr, event) -> {
            handleMouseClicked((int) event.x(), (int) event.y(), event.button());
            return true;
        });
        ScreenMouseEvents.allowMouseRelease(this).register((scr, event) -> {
            handleMouseReleased();
            return true;
        });
        ScreenMouseEvents.allowMouseDrag(this).register((scr, event, dx, dy) -> {
            handleMouseDragged((int) event.x(), (int) event.y());
            return true;
        });
        ScreenMouseEvents.allowMouseScroll(this).register((scr, mx, my, hScroll, vScroll) -> {
            handleMouseScrolled((int) mx, (int) my, hScroll, vScroll, shiftHeld);
            return true;
        });
        ScreenKeyboardEvents.allowKeyPress(this).register((scr, event) -> {
            if ((event.modifiers() & GLFW.GLFW_MOD_SHIFT) != 0) shiftHeld = true;
            handleKeyPressed(event.key(), event.scancode(), event.modifiers()); return true;
        });
        ScreenKeyboardEvents.allowKeyRelease(this).register((scr, event) -> {
            if (event.key() == 340 || event.key() == 344) shiftHeld = false;
            handleKeyReleased(event.key());
            return true;
        });
    }

    public boolean charTyped(char c, int mods) {
        handleCharTyped(c, mods);
        return true;
    }

    private void buildPanels() {
        int startX = 4, startY = 4;
        // Index-aware fallback: row Y derived from the panel's own index, NOT a
        // counter that only ticks when fallback fires. With the old counter, a
        // panel's fallback row depended on how many earlier panels lacked saved
        // positions, which changed as users dragged some panels but not others.
        java.util.function.IntFunction<int[]> stackPos = i -> new int[]{startX, startY + i * (HEADER_H + 1)};
        // Custom default position for the Disco Mode panel: place it next to
        // Manual Pest (right-hand column, same row) instead of stacking it
        // below all other panels where it would be hidden behind the column.
        Supplier<int[]> discoDefaultPos = () -> new int[]{startX + PANEL_W + 8, startY + 4 * (HEADER_H + 1)};
        int[][] saved = MacroConfig.clickGuiPanelPositions;
        panels.add(generalPanel(pos(saved, 0, () -> stackPos.apply(0))));
        panels.add(delaysPanel(pos(saved, 1, () -> stackPos.apply(1))));
        panels.add(wardrobePanel(pos(saved, 2, () -> stackPos.apply(2))));
        panels.add(autoPestPanel(pos(saved, 3, () -> stackPos.apply(3))));
        panels.add(manualPestPanel(pos(saved, 4, () -> stackPos.apply(4))));
        panels.add(profitPanel(pos(saved, 5, () -> stackPos.apply(5))));
        panels.add(dynamicRestPanel(pos(saved, 6, () -> stackPos.apply(6))));
        panels.add(qolPanel(pos(saved, 7, () -> stackPos.apply(7))));
        panels.add(themePanel(pos(saved, 8, () -> stackPos.apply(8))));
        panels.add(chatRulesPanel(pos(saved, 9, () -> stackPos.apply(9))));
        panels.add(discoModePanel(pos(saved, 10, discoDefaultPos)));
    }

    private static int[] pos(int[][] saved, int i, Supplier<int[]> fallback) {
        if (saved != null && i < saved.length && saved[i] != null && saved[i].length >= 2
                && (saved[i][0] != 0 || saved[i][1] != 0)) return saved[i];
        return fallback.get();
    }

    private static Panel makePanel(String title, int[] pos) {
        Panel p = new Panel(title, pos[0], pos[1]);
        if (pos.length >= 3) p.collapsed = pos[2] == 1;
        return p;
    }

    private void rebuildPanels() {
        panels.clear();
        buildPanels();
    }


    private Panel generalPanel(int[] pos) {
        Panel p = makePanel(tr("General", "常规"), pos);
        p.add(cycleEnum(tr("Language", "语言"), MacroConfig.Language.values(), () -> MacroConfig.language, v -> {
            MacroConfig.language = v;
            save();
            rebuildPanels();
        }));
        p.add(toggle(tr("Show Macro HUD", "显示宏 HUD"), () -> MacroConfig.showHud, v -> { MacroConfig.showHud = v; save(); }));
        p.add(toggle(tr("GUI Only in Garden", "仅在花园显示 GUI"), () -> MacroConfig.guiOnlyInGarden, v -> { MacroConfig.guiOnlyInGarden = v; save(); }));
        p.add(toggle(tr("Enable PlotTP Rewarp", "启用 PlotTP 回传"), () -> MacroConfig.enablePlotTpRewarp, v -> { MacroConfig.enablePlotTpRewarp = v; save(); }));
        p.add(toggle(tr("Hold W Until Wall", "按住 W 直到撞墙"), () -> MacroConfig.holdWUntilWall, v -> { MacroConfig.holdWUntilWall = v; save(); }));
        p.add(cycleEnum(tr("Unfly Mode", "取消飞行模式"), MacroConfig.UnflyMode.values(), () -> MacroConfig.unflyMode, v -> { MacroConfig.unflyMode = v; save(); }));
        p.add(new ScriptSelectorEntry());
        p.add(textSetting(tr("PlotTP Number", "PlotTP 编号"), "plotTpNumber", () -> MacroConfig.plotTpNumber, v -> { MacroConfig.plotTpNumber = v; save(); }));
        p.add(button(tr("Capture Rewarp Pos", "记录回传坐标"), () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                MacroConfig.rewarpEndX = mc.player.getX();
                MacroConfig.rewarpEndY = mc.player.getY();
                MacroConfig.rewarpEndZ = mc.player.getZ();
                MacroConfig.rewarpEndPosSet = true;
                save();
                mc.player.displayClientMessage(Component.literal(tr("Rewarp position captured!", "已记录回传坐标！")), true);
            }
        }));
        p.add(toggle(tr("Auto-Resume After Rest", "休息后自动继续"), () -> MacroConfig.autoResumeAfterDynamicRest, v -> { MacroConfig.autoResumeAfterDynamicRest = v; save(); }));
        p.add(toggle(tr("Auto-Recover Disconnect", "断线后自动恢复"), () -> MacroConfig.autoRecoverUnexpectedDisconnect, v -> { MacroConfig.autoRecoverUnexpectedDisconnect = v; save(); }));
        p.add(toggle(tr("Persist Session Timer", "保留会话计时器"), () -> MacroConfig.persistSessionTimer, v -> { MacroConfig.persistSessionTimer = v; save(); }));

        SectionEntry visitorSection = new SectionEntry(tr("Auto Visitor", "自动访客"), "autovisitor");
        visitorSection.add(toggle(tr("Auto-Visitor", "自动访客"), () -> MacroConfig.autoVisitor, v -> { MacroConfig.autoVisitor = v; save(); }));
        visitorSection.add(slider(tr("Threshold", "阈值"), "visitorThreshold", 1, 5, () -> MacroConfig.visitorThreshold, v -> { MacroConfig.visitorThreshold = v; save(); }, ""));
        p.add(visitorSection);

        SectionEntry georgeSection = new SectionEntry(tr("Auto George", "自动 George"), "autogeorge");
        georgeSection.add(toggle(tr("Auto George Sell", "自动 George 出售"), () -> MacroConfig.autoGeorgeSell, v -> { MacroConfig.autoGeorgeSell = v; save(); }));
        georgeSection.add(slider(tr("Threshold", "阈值"), "georgeSellThreshold", 1, 35, () -> MacroConfig.georgeSellThreshold, v -> { MacroConfig.georgeSellThreshold = v; save(); }, ""));
        p.add(georgeSection);

        SectionEntry sellSection = new SectionEntry(tr("Auto Sell", "自动出售"), "autosell");
        sellSection.add(toggle(tr("Custom Autosell", "自定义自动出售"), () -> MacroConfig.autoBoosterCookie, v -> { MacroConfig.autoBoosterCookie = v; save(); }));
        sellSection.add(listSetting(tr("Autosell Item List", "自动出售物品列表"), "boosterCookieItems", () -> MacroConfig.boosterCookieItems, v -> { MacroConfig.boosterCookieItems = new ArrayList<>(v); save(); }));
        p.add(sellSection);

        return p;
    }

    private Panel delaysPanel(int[] pos) {
        Panel p = makePanel(tr("Delays", "延迟"), pos);
        p.add(slider(tr("Rand Delay", "随机延迟"), "additionalRandomDelay", 0, 1000, () -> MacroConfig.additionalRandomDelay, v -> {
            MacroConfig.additionalRandomDelay = v;
            save();
        }, "ms"));
        p.add(slider(tr("Rotation", "旋转"), "rotationTime", 100, 3000, () -> MacroConfig.rotationTime, v -> {
            MacroConfig.rotationTime = v;
            save();
        }, "ms"));
        p.add(slider(tr("GUI Click", "GUI 点击"), "guiClickDelay", 100, 2000, () -> MacroConfig.guiClickDelay, v -> {
            MacroConfig.guiClickDelay = v;
            save();
        }, "ms"));
        p.add(slider(tr("Equip Swap", "装备切换"), "equipmentSwapDelay", 100, 300, () -> MacroConfig.equipmentSwapDelay, v -> {
            MacroConfig.equipmentSwapDelay = v;
            save();
        }, "ms"));
        p.add(slider(tr("Setspawn Cooldown", "重生点冷却"), "setspawnCooldown", 250, 3000, () -> MacroConfig.setspawnCooldown, v -> {
            MacroConfig.setspawnCooldown = v;
            save();
        }, "ms"));
        p.add(slider(tr("Rod Swap", "鱼竿切换"), "rodSwapDelay", 50, 1000, () -> MacroConfig.rodSwapDelay, v -> {
            MacroConfig.rodSwapDelay = v;
            save();
        }, "ms"));
        p.add(slider(tr("Pest Chat", "虫害聊天"), "pestChatTriggerDelay", 0, 3000, () -> MacroConfig.pestChatTriggerDelay, v -> {
            MacroConfig.pestChatTriggerDelay = v;
            save();
        }, "ms"));
        p.add(slider(tr("Book Combine", "书本合成"), "bookCombineDelay", 100, 2000, () -> MacroConfig.bookCombineDelay, v -> {
            MacroConfig.bookCombineDelay = v;
            save();
        }, "ms"));
        p.add(slider(tr("Autosell Click", "自动出售点击"), "autosellClickDelay", 100, 2000, () -> MacroConfig.autosellClickDelay, v -> {
            MacroConfig.autosellClickDelay = v;
            save();
        }, "ms"));
        p.add(slider(tr("Wardrobe Post-Swap", "衣柜后切换"), "wardrobePostSwapDelay", 0, 2000, () -> MacroConfig.wardrobePostSwapDelay, v -> {
            MacroConfig.wardrobePostSwapDelay = v;
            save();
        }, "ms"));
        p.add(slider(tr("Wardrobe AOTV", "衣柜 AOTV"), "wardrobeAotvDelay", 0, 2000, () -> MacroConfig.wardrobeAotvDelay, v -> {
            MacroConfig.wardrobeAotvDelay = v;
            save();
        }, "ms"));
        p.add(slider(tr("AOTV Vacuum", "AOTV 吸附"), "aotvVacuumDelay", 0, 2000, () -> MacroConfig.aotvVacuumDelay, v -> {
            MacroConfig.aotvVacuumDelay = v;
            save();
        }, "ms"));
        return p;
    }

    private Panel wardrobePanel(int[] pos) {
        Panel p = makePanel(tr("Wardrobe Swap", "衣柜切换"), pos);
        p.add(toggle(tr("Auto Wardrobe (Pest)", "自动衣柜（虫害）"), () -> MacroConfig.autoWardrobePest, v -> {
            MacroConfig.autoWardrobePest = v;
            save();
        }));
        p.add(toggle(tr("Auto Wardrobe (Visitor)", "自动衣柜（访客）"), () -> MacroConfig.autoWardrobeVisitor, v -> {
            MacroConfig.autoWardrobeVisitor = v;
            save();
        }));
        p.add(toggle(tr("Armor Swap (Visitor)", "护甲切换（访客）"), () -> MacroConfig.armorSwapVisitor, v -> {
            MacroConfig.armorSwapVisitor = v;
            save();
        }));
        p.add(slider(tr("Farming Slot", "耕作槽位"), "wardrobeSlotFarming", 1, 9, () -> MacroConfig.wardrobeSlotFarming, v -> {
            MacroConfig.wardrobeSlotFarming = v;
            save();
        }, ""));
        p.add(slider(tr("Pest Slot", "虫害槽位"), "wardrobeSlotPest", 1, 9, () -> MacroConfig.wardrobeSlotPest, v -> {
            MacroConfig.wardrobeSlotPest = v;
            save();
        }, ""));
        p.add(slider(tr("Visitor Slot", "访客槽位"), "wardrobeSlotVisitor", 1, 9, () -> MacroConfig.wardrobeSlotVisitor, v -> {
            MacroConfig.wardrobeSlotVisitor = v;
            save();
        }, ""));
        return p;
    }

    private Panel autoRodPanel(int[] pos) {
        Panel p = makePanel(tr("Auto Rod", "自动鱼竿"), pos);
        p.add(toggle(tr("Rod on Pest CD", "虫害 CD 时甩竿"), () -> MacroConfig.autoRodPestCd, v -> {
            MacroConfig.autoRodPestCd = v;
            save();
        }));
        p.add(toggle(tr("Rod on Pest Spawn", "虫害生成时甩竿"), () -> MacroConfig.autoRodPestSpawn, v -> {
            MacroConfig.autoRodPestSpawn = v;
            save();
        }));
        p.add(toggle(tr("Rod on Return to Farm", "返回农场时甩竿"), () -> MacroConfig.autoRodReturnToFarm, v -> {
            MacroConfig.autoRodReturnToFarm = v;
            save();
        }));
        return p;
    }

    private Panel equipmentGeorgePanel(int[] pos) {
        Panel p = makePanel(tr("Equipment & George", "装备与 George"), pos);
        p.add(toggle(tr("Auto-Equipment", "自动装备"), () -> MacroConfig.autoEquipment, v -> {
            MacroConfig.autoEquipment = v;
            save();
        }));
        p.add(toggle(tr("Auto George Sell", "自动 George 出售"), () -> MacroConfig.autoGeorgeSell, v -> {
            MacroConfig.autoGeorgeSell = v;
            save();
        }));
        p.add(slider(tr("George Threshold", "George 阈值"), "georgeSellThreshold", 1, 35, () -> MacroConfig.georgeSellThreshold, v -> {
            MacroConfig.georgeSellThreshold = v;
            save();
        }, ""));
        return p;
    }

    private Panel autoPestPanel(int[] pos) {
        Panel p = makePanel(tr("Auto Pest", "自动虫害"), pos);
        p.add(toggle(tr("Enabled", "启用"), () -> MacroConfig.autoPestEnabled, v -> {
            MacroConfig.autoPestEnabled = v;
            save();
        }));
        p.add(slider(tr("Threshold", "阈值"), "pestThreshold", 1, 8, () -> MacroConfig.pestThreshold, v -> {
            MacroConfig.pestThreshold = v;
            save();
        }, ""));
        p.add(toggle(tr("Trigger on Chat", "聊天触发"), () -> MacroConfig.triggerPestOnChat, v -> {
            MacroConfig.triggerPestOnChat = v;
            save();
        }));
        p.add(toggle(tr("Delay Crop Fever", "延后作物狂热"), () -> MacroConfig.delayPestForCropFever, v -> {
            MacroConfig.delayPestForCropFever = v;
            save();
        }));
        p.add(toggle(tr("AOTV to Roof", "AOTV 到屋顶"), () -> MacroConfig.aotvToRoof, v -> {
            MacroConfig.aotvToRoof = v;
            save();
        }));
        p.add(csvTextSetting(tr("AOTV Roof Plots", "AOTV 屋顶地块"), "aotvRoofPlots",
                () -> String.join(", ", MacroConfig.aotvRoofPlots),
                v -> {
                    List<String> plots = new ArrayList<>();
                    for (String part : v.split(",")) {
                        String trimmed = part.trim();
                        if (!trimmed.isEmpty()) plots.add(trimmed);
                    }
                    MacroConfig.aotvRoofPlots = plots;
                    save();
                },
                tr("Comma separated, e.g. 1, 2, 3", "用逗号分隔，例如 1, 2, 3")));
        p.add(toggle(tr("Break Before AOTV", "AOTV 前破坏方块"), () -> MacroConfig.breakBlocksBeforeAotv, v -> {
            MacroConfig.breakBlocksBeforeAotv = v;
            save();
        }));
        p.add(toggle(tr("Call Phillip for Bonus", "呼叫 Phillip 领取加成"), () -> MacroConfig.callPhillipForBonus, v -> {
            MacroConfig.callPhillipForBonus = v;
            save();
        }));
        p.add(toggle(tr("Spray Single Plot", "喷洒单个地块"), () -> MacroConfig.spraySinglePlot, v -> {
            MacroConfig.spraySinglePlot = v;
            save();
        }));
        p.add(slider(tr("Roof Pitch", "屋顶仰角"), "aotvRoofPitch", 45, 90, () -> MacroConfig.aotvRoofPitch, v -> {
            MacroConfig.aotvRoofPitch = v;
            save();
        }, ""));
        p.add(slider(tr("Pitch Human.", "仰角拟人化"), "aotvRoofPitchHumanization", 0, 15, () -> MacroConfig.aotvRoofPitchHumanization, v -> {
            MacroConfig.aotvRoofPitchHumanization = v;
            save();
        }, ""));
        SectionEntry rodSection = new SectionEntry(tr("Auto Rod", "自动鱼竿"), "autorod");
        rodSection.add(toggle(tr("Rod on Pest CD", "虫害 CD 时甩竿"), () -> MacroConfig.autoRodPestCd, v -> { MacroConfig.autoRodPestCd = v; save(); }));
        rodSection.add(toggle(tr("Rod on Pest Spawn", "虫害生成时甩竿"), () -> MacroConfig.autoRodPestSpawn, v -> { MacroConfig.autoRodPestSpawn = v; save(); }));
        rodSection.add(toggle(tr("Rod on Return to Farm", "返回农场时甩竿"), () -> MacroConfig.autoRodReturnToFarm, v -> { MacroConfig.autoRodReturnToFarm = v; save(); }));
        p.add(rodSection);
        SectionEntry equipSection = new SectionEntry(tr("Equipment Swap", "装备切换"), "equipmentswap");
        equipSection.add(toggle(tr("Auto-Equipment", "自动装备"), () -> MacroConfig.autoEquipment, v -> { MacroConfig.autoEquipment = v; save(); }));
        p.add(equipSection);
        return p;
    }

    private Panel manualPestPanel(int[] pos) {
        Panel p = makePanel(tr("Manual Pest", "手动虫害"), pos);
        p.add(toggle(tr("Manual Clean", "手动清理"), () -> MacroConfig.manualPestClean, v -> {
            MacroConfig.manualPestClean = v;
            save();
        }));
        p.add(toggle(tr("Manual Ding", "手动提示音"), () -> MacroConfig.manualPestAlertSound, v -> {
            MacroConfig.manualPestAlertSound = v;
            save();
        }));
        p.add(textSetting(tr("Custom Sound", "自定义声音"), "manualPestSoundPath", () -> MacroConfig.manualPestSoundPath, v -> {
            MacroConfig.manualPestSoundPath = v;
            save();
        }));
        p.add(slider(tr("Manual Return", "手动返回"), "manualPestReturnDelay", 0, 10000, () -> MacroConfig.manualPestReturnDelay, v -> {
            MacroConfig.manualPestReturnDelay = v;
            save();
        }, ""));
        p.add(slider(tr("Rewarp At", "回传于"), "manualPestRewarpAt", 0, 8, () -> MacroConfig.manualPestRewarpAt, v -> {
            MacroConfig.manualPestRewarpAt = v;
            save();
        }, ""));
        return p;
    }

    private Panel discoModePanel(int[] pos) {
        Panel p = makePanel(tr("Disco Mode", "迪斯科模式"), pos);
        p.add(toggle(tr("Disco Destination", "迪斯科目的地"), () -> MacroConfig.discoDestinationMode, v -> {
            MacroConfig.discoDestinationMode = v;
            save();
        }));
        p.add(toggle(tr("Tp Before Wardrobe", "传送先于换装"), () -> MacroConfig.discoTpBeforeWardrobe, v -> {
            MacroConfig.discoTpBeforeWardrobe = v;
            save();
        }));
        p.add(slider(tr("Disco Return", "迪斯科返回"), "discoReturnDelay", 0, 10000, () -> MacroConfig.discoReturnDelay, v -> {
            MacroConfig.discoReturnDelay = v;
            save();
        }, ""));
        p.add(slider(tr("Rewarp At", "回传于"), "discoRewarpAt", 0, 8, () -> MacroConfig.discoRewarpAt, v -> {
            MacroConfig.discoRewarpAt = v;
            save();
        }, ""));
        return p;
    }

    private Panel autoVisitorPanel(int[] pos) {
        Panel p = makePanel(tr("Auto Visitor", "自动访客"), pos);
        p.add(toggle(tr("Auto-Visitor", "自动访客"), () -> MacroConfig.autoVisitor, v -> {
            MacroConfig.autoVisitor = v;
            save();
        }));
        p.add(slider(tr("Threshold", "阈值"), "visitorThreshold", 1, 5, () -> MacroConfig.visitorThreshold, v -> {
            MacroConfig.visitorThreshold = v;
            save();
        }, ""));
        return p;
    }



    private Panel autoSellPanel(int[] pos) {
        Panel p = makePanel(tr("Auto Sell", "自动出售"), pos);
        p.add(toggle(tr("Custom Autosell", "自定义自动出售"), () -> MacroConfig.autoBoosterCookie, v -> {
            MacroConfig.autoBoosterCookie = v;
            save();
        }));
        p.add(listSetting(tr("Autosell Item List", "自动出售物品列表"), "boosterCookieItems", () -> MacroConfig.boosterCookieItems,
                v -> {
                    MacroConfig.boosterCookieItems = new ArrayList<>(v);
                    save();
                }));
        return p;
    }

    private Panel profitPanel(int[] pos) {
        Panel p = makePanel(tr("Profit Calculator", "利润计算器"), pos);
        p.add(toggle(tr("Session HUD", "会话 HUD"), () -> MacroConfig.showSessionProfitHud, v -> {
            MacroConfig.showSessionProfitHud = v;
            save();
        }));
        p.add(toggle(tr("Daily HUD", "每日 HUD"), () -> MacroConfig.showTotalToday, v -> {
            MacroConfig.showTotalToday = v;
            save();
        }));
        p.add(toggle(tr("Lifetime HUD", "总计 HUD"), () -> MacroConfig.showLifetimeHud, v -> {
            MacroConfig.showLifetimeHud = v;
            save();
        }));
        p.add(toggle(tr("HUD While Off", "关闭时显示 HUD"), () -> MacroConfig.showProfitHudWhileInactive, v -> {
            MacroConfig.showProfitHudWhileInactive = v;
            save();
        }));
        p.add(toggle(tr("Compact", "紧凑模式"), () -> MacroConfig.compactProfitCalculator, v -> {
            MacroConfig.compactProfitCalculator = v;
            save();
        }));
        p.add(cycleEnum(tr("Pricing Mode", "定价模式"), MacroConfig.PricingMode.values(), () -> MacroConfig.pricingMode, v -> { MacroConfig.pricingMode = v; save(); }));
        p.add(listSetting(tr("Pet XP List", "宠物经验列表"), "petXpTrackedPets", () -> MacroConfig.petXpTrackedPets,
                v -> {
                    MacroConfig.petXpTrackedPets = new ArrayList<>(v);
                    ProfitManager.refreshConfiguredPetXpPrices();
                    save();
                },
                tr("Pet Name, Max Level (100/200), Level 1 Price,", "宠物名称，最高等级（100/200），1级价格，"),
                tr("Max Level Price, Rarity", "满级价格，稀有度") ));
        p.add(button(tr("Reset Session", "重置会话"), () -> {
            MacroStateManager.resetSession();
            notifyMsg(tr("Session reset!", "会话已重置！"));
        }));
        p.add(button(tr("Reset Daily", "重置每日"), () -> {
            MacroConfig.todayAccumulatedMs = 0;
            MacroConfig.todayDateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            com.ihanuat.mod.modules.TodayTimeTracker.syncFromConfig();
            if (MacroStateManager.isMacroRunning()) com.ihanuat.mod.modules.TodayTimeTracker.onMacroStart();
            MacroConfig.save();
            ProfitManager.resetDaily();
            notifyMsg(tr("Daily reset!", "每日已重置！"));
        }));
        p.add(button(tr("Reset Lifetime", "重置总计"), () -> {
            MacroConfig.lifetimeAccumulated = 0;
            MacroStateManager.syncFromConfig();
            MacroConfig.save();
            ProfitManager.resetLifetime();
            notifyMsg(tr("Lifetime reset!", "总计已重置！"));
        }));
        return p;
    }

    private Panel dynamicRestPanel(int[] pos) {
        Panel p = makePanel(tr("Dynamic Rest", "动态休息"), pos);
        p.add(intField(tr("Farming Min", "耕作最小值"), "restScriptingTimeMin", () -> MacroConfig.restScriptingTimeMin, v -> {
            MacroConfig.restScriptingTimeMin = Math.max(1, v);
            com.ihanuat.mod.modules.DynamicRestManager.onConfigChanged();
            save();
        }, "min"));
        p.add(intField(tr("Farming Max", "耕作最大值"), "restScriptingTimeMax", () -> MacroConfig.restScriptingTimeMax, v -> {
            MacroConfig.restScriptingTimeMax = Math.max(1, v);
            com.ihanuat.mod.modules.DynamicRestManager.onConfigChanged();
            save();
        }, "min"));
        p.add(intField(tr("Break Min", "休息最小值"), "restBreakTimeMin", () -> MacroConfig.restBreakTimeMin, v -> {
            MacroConfig.restBreakTimeMin = Math.max(1, v);
            save();
        }, "min"));
        p.add(intField(tr("Break Max", "休息最大值"), "restBreakTimeMax", () -> MacroConfig.restBreakTimeMax, v -> {
            MacroConfig.restBreakTimeMax = Math.max(1, v);
            save();
        }, "min"));
        p.add(toggle(tr("Show Daily Total", "显示今日总时长"), () -> MacroConfig.showTotalToday, v -> {
            MacroConfig.showTotalToday = v;
            save();
        }));
        p.add(toggle(tr("Show Total Farmed", "显示累计总时长"), () -> MacroConfig.showTotalFarmed, v -> {
            MacroConfig.showTotalFarmed = v;
            save();
        }));
        p.add(toggle(tr("Supercraft Before Rest", "休息前超级合成"), () -> MacroConfig.superCraftBeforeRest, v -> {
            MacroConfig.superCraftBeforeRest = v;
            save();
        }));
        p.add(listSetting(tr("Supercraft Crops", "超级合作物"), "superCraftCrops", () -> MacroConfig.superCraftCrops, v -> {
                MacroConfig.superCraftCrops = new ArrayList<>(v);
                save();
        }));
        p.add(doubleField(tr("Quit Threshold", "退出阈值"), "quitThresholdHours", () -> MacroConfig.quitThresholdHours, v -> {
            MacroConfig.quitThresholdHours = Math.max(0.0, v);
            save();
        }, "hr"));
        p.add(toggle(tr("Reset Timer At 00:00", "在 00:00 重置计时器"), () -> MacroConfig.quitAfterSessionLength, v -> {
            MacroConfig.quitAfterSessionLength = v;
            com.ihanuat.mod.modules.QuitThresholdManager.syncFromConfig();
            save();
        }));
        p.add(button(tr("Reset Quit Timer", "重置退出计时器"), () -> {
            com.ihanuat.mod.modules.QuitThresholdManager.resetThreshold();
            notifyMsg(tr("Quit timer reset!", "退出计时器已重置！"));
        }));
        p.add(toggle(tr("Force Quit MC", "强制关闭 MC"), () -> MacroConfig.forceQuitMinecraft, v -> {
            MacroConfig.forceQuitMinecraft = v;
            save();
        }));
        return p;
    }

    private Panel qolPanel(int[] pos) {
        Panel p = makePanel(tr("QOL", "生活质量"), pos);
        p.add(toggle(tr("Book Combine", "书本合成"), () -> MacroConfig.autoBookCombine, v -> {
            MacroConfig.autoBookCombine = v;
            save();
        }));
        p.add(toggle(tr("Always Combine", "始终合成"), () -> MacroConfig.alwaysActiveCombine, v -> {
            MacroConfig.alwaysActiveCombine = v;
            save();
        }));
        p.add(slider(tr("Book Threshold", "书本阈值"), "bookThreshold", 1, 35, () -> MacroConfig.bookThreshold, v -> {
            MacroConfig.bookThreshold = v;
            save();
        }, ""));
        p.add(toggle(tr("Chat Cleanup", "聊天清理"), () -> MacroConfig.hideFilteredChat, v -> {
            MacroConfig.hideFilteredChat = v;
            save();
        }));
        p.add(toggle(tr("Auto-Drop Junk", "自动丢弃垃圾"), () -> MacroConfig.autoDropJunk, v -> {
            MacroConfig.autoDropJunk = v;
            save();
        }));
        p.add(listSetting(tr("Junk List", "垃圾列表"), "junkItems", () -> MacroConfig.junkItems,
                v -> {
                    MacroConfig.junkItems = new ArrayList<>(v);
                    save();
                }));
        p.add(slider(tr("Junk Threshold", "垃圾阈值"), "junkThreshold", 1, 35, () -> MacroConfig.junkThreshold, v -> {
            MacroConfig.junkThreshold = v;
            save();
        }, ""));
        p.add(textSetting(tr("Junk PlotTP", "垃圾 PlotTP"), "dropJunkPlotTp", () -> MacroConfig.dropJunkPlotTp, v -> {
            MacroConfig.dropJunkPlotTp = v;
            save();
        }));
        p.add(toggle(tr("Stash Manager", "仓库管理"), () -> MacroConfig.autoStashManager, v -> {
            MacroConfig.autoStashManager = v;
            save();
        }));
        p.add(toggle(tr("Discord Status", "Discord 状态"), () -> MacroConfig.sendDiscordStatus, v -> {
            MacroConfig.sendDiscordStatus = v;
            save();
        }));
        p.add(textSetting(tr("Webhook URL", "Webhook 地址"), "discordWebhookUrl", () -> MacroConfig.discordWebhookUrl, v -> {
            MacroConfig.discordWebhookUrl = v;
            save();
        }));
        p.add(intField(tr("Discord Interval", "Discord 间隔"), "discordStatusUpdateTime", () -> MacroConfig.discordStatusUpdateTime, v -> {
            MacroConfig.discordStatusUpdateTime = v;
            save();
        }, "min"));
        p.add(toggle(tr("Debug Messages", "调试消息"), () -> MacroConfig.showDebug, v -> {
            MacroConfig.showDebug = v;
            save();
        }));
        p.add(toggle(tr("Log to File", "记录到文件"), () -> MacroConfig.logDebugToFile, v -> {
            MacroConfig.logDebugToFile = v;
            if (!v) com.ihanuat.mod.DebugLogger.getInstance().close();
            save();
        }));
        p.add(button(tr("Open Log Folder", "打开日志文件夹"), () -> {
            try {
                java.awt.Desktop.getDesktop().open(net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir().toFile());
            } catch (Exception e) {
                notifyMsg(tr("Failed: ", "失败：") + e.getMessage());
            }
        }));
        return p;
    }

    private Panel themePanel(int[] pos) {
        Panel p = makePanel(tr("Theme", "主题"), pos);
        p.add(new ThemeLibraryEntry());
        p.add(button(tr("Copy Current Theme", "复制当前主题"), () -> {
            String code = encodeTheme();
            Minecraft mc = Minecraft.getInstance();
            mc.keyboardHandler.setClipboard(code);
            notifyMsg(tr("Theme code copied!", "主题代码已复制！"));
        }));
        p.add(cycleEnum(tr("Text Style", "文字样式"), MacroConfig.TextStyle.values(), () -> MacroConfig.themeTextStyle, v -> { MacroConfig.themeTextStyle = v; save(); }));
        p.add(slider(tr("Outline Size", "描边大小"), "themeOutlineSize",   1, 3, () -> MacroConfig.themeOutlineSize,   v -> { MacroConfig.themeOutlineSize = v;   save(); }, "px"));
        p.add(slider(tr("Shadow Opacity", "阴影不透明度"), "themeShadowOpacity", 0, 255, () -> MacroConfig.themeShadowOpacity, v -> { MacroConfig.themeShadowOpacity = v; save(); }, ""));
        p.add(colorEntry(tr("Panel BG", "面板背景"), () -> MacroConfig.themePanelBg, v -> {
            MacroConfig.themePanelBg = v;
            save();
        }));
        p.add(colorEntry(tr("Panel Header", "面板标题栏"), () -> MacroConfig.themePanelHeader, v -> {
            MacroConfig.themePanelHeader = v;
            save();
        }));
        p.add(colorEntry(tr("Accent", "强调色"), () -> MacroConfig.themeAccent, v -> {
            MacroConfig.themeAccent = v;
            save();
        }));
        p.add(colorEntry(tr("Text", "文字"), () -> MacroConfig.themeText, v -> {
            MacroConfig.themeText = v;
            save();
        }));
        p.add(colorEntry(tr("Text Dimmed", "次要文字"), () -> MacroConfig.themeTextDim, v -> {
            MacroConfig.themeTextDim = v;
            save();
        }));
        p.add(colorEntry(tr("Toggle ON", "开关开启"), () -> MacroConfig.themeToggleOn, v -> {
            MacroConfig.themeToggleOn = v;
            save();
        }));
        p.add(colorEntry(tr("Toggle OFF", "开关关闭"), () -> MacroConfig.themeToggleOff, v -> {
            MacroConfig.themeToggleOff = v;
            save();
        }));
        p.add(colorEntry(tr("Slider Fill", "滑块填充"), () -> MacroConfig.themeSliderFill, v -> {
            MacroConfig.themeSliderFill = v;
            save();
        }));
        p.add(colorEntry(tr("Button Hover", "按钮悬停"), () -> MacroConfig.themeButtonHover, v -> {
            MacroConfig.themeButtonHover = v;
            save();
        }));
        p.add(colorEntryNoAlpha(tr("HUD Background", "HUD 背景"), () -> MacroConfig.toArgb(MacroConfig.hudBgColor), v -> {
            MacroConfig.hudBgColor = v & 0xFFFFFF;
            save();
        }));
        p.add(colorEntryNoAlpha(tr("HUD Accent", "HUD 强调色"), () -> MacroConfig.toArgb(MacroConfig.hudAccentColor), v -> {
            MacroConfig.hudAccentColor = v & 0xFFFFFF;
            save();
        }));
        p.add(colorEntryNoAlpha(tr("HUD Title", "HUD 标题"), () -> MacroConfig.toArgb(MacroConfig.hudTitleColor), v -> {
            MacroConfig.hudTitleColor = v & 0xFFFFFF;
            save();
        }));
        p.add(colorEntryNoAlpha(tr("HUD Label", "HUD 标签"), () -> MacroConfig.toArgb(MacroConfig.hudLabelColor), v -> {
            MacroConfig.hudLabelColor = v & 0xFFFFFF;
            save();
        }));
        p.add(colorEntryNoAlpha(tr("HUD Value", "HUD 数值"), () -> MacroConfig.toArgb(MacroConfig.hudValueColor), v -> {
            MacroConfig.hudValueColor = v & 0xFFFFFF;
            save();
        }));
        p.add(colorEntryNoAlpha(tr("HUD Bar BG", "HUD 条背景"), () -> MacroConfig.toArgb(MacroConfig.hudBarBgColor), v -> {
            MacroConfig.hudBarBgColor = v & 0xFFFFFF;
            save();
        }));
        p.add(colorEntryNoAlpha(tr("HUD Bar Fill", "HUD 条填充"), () -> MacroConfig.toArgb(MacroConfig.hudBarFillColor), v -> {
            MacroConfig.hudBarFillColor = v & 0xFFFFFF;
            save();
        }));
        p.add(colorEntryNoAlpha(tr("HUD State Off", "HUD 状态关闭"), () -> MacroConfig.toArgb(MacroConfig.hudStateOffColor), v -> {
            MacroConfig.hudStateOffColor = v & 0xFFFFFF;
            save();
        }));
        p.add(colorEntryNoAlpha(tr("HUD State Farm", "HUD 状态耕作"), () -> MacroConfig.toArgb(MacroConfig.hudStateFarmingColor), v -> {
            MacroConfig.hudStateFarmingColor = v & 0xFFFFFF;
            save();
        }));
        p.add(colorEntryNoAlpha(tr("HUD State Clean", "HUD 状态清理"), () -> MacroConfig.toArgb(MacroConfig.hudStateCleaningColor), v -> {
            MacroConfig.hudStateCleaningColor = v & 0xFFFFFF;
            save();
        }));
        p.add(colorEntryNoAlpha(tr("HUD State Rec", "HUD 状态恢复"), () -> MacroConfig.toArgb(MacroConfig.hudStateRecoveringColor), v -> {
            MacroConfig.hudStateRecoveringColor = v & 0xFFFFFF;
            save();
        }));
        p.add(colorEntryNoAlpha(tr("HUD State Visit", "HUD 状态访客"), () -> MacroConfig.toArgb(MacroConfig.hudStateVisitingColor), v -> {
            MacroConfig.hudStateVisitingColor = v & 0xFFFFFF;
            save();
        }));
        p.add(colorEntryNoAlpha(tr("HUD State Sell", "HUD 状态出售"), () -> MacroConfig.toArgb(MacroConfig.hudStateAutosellingColor), v -> {
            MacroConfig.hudStateAutosellingColor = v & 0xFFFFFF;
            save();
        }));
        p.add(colorEntryNoAlpha(tr("HUD State Spray", "HUD 状态喷洒"), () -> MacroConfig.toArgb(MacroConfig.hudStateSprayingColor), v -> {
            MacroConfig.hudStateSprayingColor = v & 0xFFFFFF;
            save();
        }));
        p.add(button(tr("Reset to Default", "恢复默认"), () -> {
            MacroConfig.themePanelBg = 0xF0101018;
            MacroConfig.themePanelHeader = 0xFF18182C;
            MacroConfig.themeAccent = 0xFF5050A0;
            MacroConfig.themeText = 0xFFCCCCCC;
            MacroConfig.themeTextDim = 0xFF666677;
            MacroConfig.themeToggleOn = 0xFF4444BB;
            MacroConfig.themeToggleOff = 0xFF2A2A3A;
            MacroConfig.themeSliderFill = 0xFF3A3A99;
            MacroConfig.themeButtonHover = 0xFF4444BB;
            MacroConfig.hudBgColor = MacroConfig.DEFAULT_HUD_BG_COLOR;
            MacroConfig.hudAccentColor = MacroConfig.DEFAULT_HUD_ACCENT_COLOR;
            MacroConfig.hudTitleColor = MacroConfig.DEFAULT_HUD_TITLE_COLOR;
            MacroConfig.hudLabelColor = MacroConfig.DEFAULT_HUD_LABEL_COLOR;
            MacroConfig.hudValueColor = MacroConfig.DEFAULT_HUD_VALUE_COLOR;
            MacroConfig.hudBarBgColor = MacroConfig.DEFAULT_HUD_BAR_BG_COLOR;
            MacroConfig.hudBarFillColor = MacroConfig.DEFAULT_HUD_BAR_FILL_COLOR;
            MacroConfig.hudStateOffColor = MacroConfig.DEFAULT_HUD_STATE_OFF_COLOR;
            MacroConfig.hudStateFarmingColor = MacroConfig.DEFAULT_HUD_STATE_FARMING_COLOR;
            MacroConfig.hudStateCleaningColor = MacroConfig.DEFAULT_HUD_STATE_CLEANING_COLOR;
            MacroConfig.hudStateRecoveringColor = MacroConfig.DEFAULT_HUD_STATE_RECOVERING_COLOR;
            MacroConfig.hudStateVisitingColor = MacroConfig.DEFAULT_HUD_STATE_VISITING_COLOR;
            MacroConfig.hudStateAutosellingColor = MacroConfig.DEFAULT_HUD_STATE_AUTOSELLING_COLOR;
            MacroConfig.hudStateSprayingColor = MacroConfig.DEFAULT_HUD_STATE_SPRAYING_COLOR;
            save();
            notifyMsg(tr("Theme reset!", "主题已重置！"));
        }));
        p.add(button(tr("Reset Panel Positions", "重置面板位置"), () -> {
            MacroConfig.clickGuiPanelPositions = new int[9][3];
            save();
            panels.clear();
            buildPanels();
            notifyMsg(tr("Panel positions reset!", "面板位置已重置！"));
        }));
        return p;
    }


    private Panel chatRulesPanel(int[] pos) {
        Panel p = makePanel(tr("Chat Rules", "聊天规则"), pos);
        p.add(new ChatRulesEntry());
        return p;
    }

    private static ToggleEntry toggle(String l, Supplier<Boolean> g, Consumer<Boolean> s) {
        return new ToggleEntry(l, g, s);
    }

    private static SliderEntry slider(String l, String field, int mn, int mx, Supplier<Integer> g, Consumer<Integer> s, String u) {
        return new SliderEntry(l, field, mn, mx, g, s, u);
    }

    private static <E extends Enum<E>> CycleEnumEntry<E> cycleEnum(String l, E[] vs, Supplier<E> g, Consumer<E> s) {
        return new CycleEnumEntry<>(l, vs, g, s);
    }

    private static TextSettingEntry textSetting(String l, String field, Supplier<String> g, Consumer<String> s) {
        return new TextSettingEntry(l, field, g, s);
    }

    private static TextSettingEntry csvTextSetting(String l, String field, Supplier<String> g, Consumer<String> s, String placeholder) {
        return new TextSettingEntry(l, field, g, s, placeholder);
    }

    private static ListSettingEntry listSetting(String l, String field, Supplier<List<String>> g, Consumer<List<String>> s) {
        return new ListSettingEntry(l, field, g, s);
    }

    private static ListSettingEntry listSetting(String l, String field, Supplier<List<String>> g, Consumer<List<String>> s, String... hints) {
        return new ListSettingEntry(l, field, g, s, hints);
    }

    private static IntFieldEntry intField(String l, String field, Supplier<Integer> g, Consumer<Integer> s, String u) {
        return new IntFieldEntry(l, field, g, s, u);
    }

    private static DoubleFieldEntry doubleField(String l, String field, Supplier<Double> g, Consumer<Double> s, String u) {
        return new DoubleFieldEntry(l, field, g, s, u);
    }

    private static ButtonEntry button(String l, Runnable a) {
        return new ButtonEntry(l, a);
    }

    private static Entry visibleWhen(Supplier<Boolean> visible, Entry entry) {
        return new VisibleEntry(visible, entry);
    }

    private static ColorEntry colorEntry(String l, Supplier<Integer> g, Consumer<Integer> s) {
        return new ColorEntry(l, g, s, true);
    }
    private static ColorEntry colorEntryNoAlpha(String l, Supplier<Integer> g, Consumer<Integer> s) {
        return new ColorEntry(l, g, s, false);
    }


    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0x99000000);
        int searchW = 160;
        int searchX = width / 2 - searchW / 2;
        int searchY = 4;
        fillRoundRect(g, searchX - 1, searchY - 1, searchW + 2, SEARCH_H + 2, 4, searchActive ? C_ACC() : C_OFF());
        fillRoundRect(g, searchX, searchY, searchW, SEARCH_H, 3, C_BG());
        String searchText = searchQuery.isEmpty() && !searchActive
                ? tr("Search modules...", "搜索模块...")
                : searchQuery + (searchActive && (System.currentTimeMillis() / 500) % 2 == 0 ? "|" : "");
        g.drawString(font, searchText, searchX + 5, searchY + 4, searchQuery.isEmpty() && !searchActive ? C_DIM() : C_TXT(), false);
        for (int i = panels.size() - 1; i >= 0; i--) panels.get(i).render(g, mx, my, font, searchQuery);
        if (activeSubPanel != null) activeSubPanel.render(g, mx, my, font);
        g.drawString(font, tr("ihanuat  shift+scroll=pan", "ihanuat  shift+滚轮=平移"), 3, height - 9, 0xFF333355, false);
        renderHelperPanel(g, mx, my, font);
    }

    private static String getHelperTopicKey(String panelTitle) {
        return switch (panelTitle) {
            case "General", "常规" -> "general";
            case "Delays", "延迟" -> "delays";
            case "Dynamic Rest", "动态休息" -> "dynamicrest";
            case "QOL", "生活质量" -> "qol";
            case "Chat Rules", "聊天规则" -> "chatrules";
            case "Auto Rod", "自动鱼竿" -> "autorod";
            case "Equipment & George", "装备与 George" -> "equipmentgeorge";
            case "Auto Pest", "自动虫害" -> "autopest";
            case "Manual Pest", "手动虫害" -> "manualpest";
            case "Disco Mode", "迪斯科模式" -> "discomode";
            case "Auto Visitor", "自动访客" -> "autovisitor";
            case "Auto Sell", "自动出售" -> "autosell";
            case "Profit Calculator", "利润计算器" -> "profitcalculator";
            case "Wardrobe Swap", "衣柜切换" -> "wardrobeswap";
            default                  -> null;
        };
    }

    private static boolean hasHelperTopic(String panelTitle) {
        return getHelperTopicKey(panelTitle) != null;
    }

    private static void updateHelperForPanel(String panelTitle) {
        String newTopic = getHelperTopicKey(panelTitle);
        if (newTopic != null && !newTopic.equals(helperTopic)) {
            helperTopic      = newTopic;
            helperTitle      = panelTitle;
            helperScrollOffset = 0;
        }
    }

    private static String[] getHelperLines(String topic) {
        if (I18n.isZh()) return getChineseHelperLines(topic);
        return switch (topic) {
            case "chatrules" -> new String[]{
                    "Name — Label shown in the Discord alert.",
                    "Pattern — Exact text to scan for in each chat message.",
                    "Contains/Equals/Starts/Ends — How the pattern is matched.",
                    "Case — When lit, matching is case-sensitive.",
                    "Active — Rule only fires when lit.",
                    "Any match sends a Discord webhook alert.",
                    "Set webhook URL in QOL panel. First match wins.",
            };
            case "delays" -> new String[]{
                    "Rand Delay — Extra random ms added on top of every timed action via getRandomizedDelay(). Acts as a global humanization jitter across most sequences.",
                    "Rotation — How long the macro spends rotating to look at the crop before it starts farming. Higher values look more human but slow cycle time.",
                    "GUI Click — Throttle between each simulated click inside menus (wardrobe, anvil, George, junk drop UI). Shared across all GUI interactions.",
                    "Equip Swap — Pause after swapping your Equipment loadout slot. Gives the server time to apply the new loadout before the next action.",
                    "Setspawn Cooldown — Pause after /setspawn finishes before the macro opens the wardrobe during pest cleaning. Default 1000ms; lower if you trust your network, raise if wardrobe interactions occasionally fail.",
                    "Rod Swap — Pause after switching to the fishing rod. Only applies when Auto Rod is enabled (pest CD / pest spawn / return to farm triggers).",
                    "Pest Chat — How long to wait after a pest chat message before the macro reacts and begins the pest clean sequence. Useful for letting the chat settle.",
                    "Book Combine — Throttle between each simulated click at the anvil during book combining. Keep this above ~150ms to avoid missed clicks.",
                    "Autosell Click — Throttle between each Booster Cookie menu click during autosell. Keep above 100ms to avoid the server dropping clicks.",
                    "Wardrobe Post-Swap — Wait after the wardrobe GUI closes on the non-AOTV path. Increase this if the macro resumes farming before armor is fully equipped.",
                    "Wardrobe AOTV — Wait after the wardrobe GUI closes on the AOTV path. Usually shorter since AOTV fires immediately after; tweak if you get swap timing issues.",
                    "AOTV Vacuum — Wait between triggering the AOTV vacuum sweep and actually firing the AOTV teleport. Too low and the vacuum misses pests before the warp.",
            };
            case "dynamicrest" -> new String[]{
                    "Farming Min/Max — Randomly pick a farming session length in this minute range.",
                    "Break Min/Max — Randomly pick a break (disconnect) duration in this minute range.",
                    "Show Daily Total — Display today's active farming time in the HUD.",
                    "Show Total Farmed — Display lifetime active farming time in the HUD.",
                    "Quit Threshold — Stop the macro when the selected quit timer exceeds this many hours (0 = off).",
                    "Reset Timer At 00:00 — Use today's farming timer for quit threshold checks and reset it automatically at 00:00. Off = keep using the dedicated quit timer.",
                    "Reset Quit Timer — Clear whichever quit timer mode is currently active.",
                    "Force Quit MC — Fully close Minecraft when the quit threshold is hit.",
            };
            case "qol" -> new String[]{
                    "Book Combine — Auto-combine enchanted books at the anvil.",
                    "Always Combine — Combine even when the macro is not farming.",
                    "Book Threshold — Number of books needed before combining triggers.",
                    "Chat Cleanup — Hide noisy Hypixel script/kill messages from chat.",
                    "Auto-Drop Junk — Automatically drop items that match the Junk List.",
                    "Junk List — List of item name fragments considered junk.",
                    "Junk Threshold — Drop when this many junk items are in inventory.",
                    "Junk PlotTP — Plot number to teleport to before dropping junk.",
                    "Stash Manager — Auto-run /pickupstash after autosell completes.",
                    "Discord Status — Periodically post macro status to a webhook.",
                    "Webhook URL — Discord webhook URL for status and chat alerts.",
                    "Discord Interval — How often (minutes) to post the status update.",
                    "Debug Messages — Show verbose debug messages in chat.",
                    "Log to File — Write debug messages to a file in the game folder.",
            };
            case "autorod" -> new String[]{
                    "Rod on Pest CD — Use the fishing rod automatically when the pest cooldown expires.",
                    "Rod on Pest Spawn — Use the fishing rod as soon as pests spawn on your farm.",
                    "Rod on Return to Farm — Use the fishing rod when returning to the farm after clearing pests or handling visitors.",
            };
            case "equipmentgeorge" -> new String[]{
                    "Auto-Equipment — Automatically switch your Equipment loadout slot when transitioning between pest spawning and pest clearing/farming modes.",
                    "Auto George Sell — Automatically sell pet drops to George via his Abiphone contact (requires George contact to be unlocked).",
                    "George Threshold — Minimum number of pets in your inventory before selling to George triggers.",
            };
            case "equipmentswap" -> new String[]{
                    "Auto-Equipment — Turn on and off whether you want to change equipment for pest spawning and pest clearing/farming.",
            };
            case "autogeorge" -> new String[]{
                    "Auto George Sell — Whether to sell pet drops to George (requires George's abiphone contact).",
                    "Threshold — Minimum amount of pets in inventory needed to sell to George.",
            };
            case "autopest" -> new String[]{
                    "Enabled — Master switch for automatic pest handling. Turn this off to pause auto-starting pest runs while leaving the rest of the macro alone.",
                    "Threshold — Number of pests that must be present before the pest cleaner activates.",
                    "Trigger on Chat — Automatically start the pest cleaner when the pest spawning notification appears in chat.",
                    "Delay Crop Fever — Wait for Crop Fever to expire before clearing pests, so you don't lose the bonus.",
                    "AOTV to Roof — Use the Aspect of the Void to etherwarp up to the roof before clearing pests.",
                    "AOTV Roof Plots — Comma-separated list of plot numbers where AOTV-to-roof is possible.",
                    "Break Before AOTV — Break blocks in the way before firing the AOTV etherwarp to the roof.",
                    "Roof Pitch — How far upward (in degrees) to look when aiming the AOTV etherwarp at the roof.",
                    "Pitch Human. — Randomize the roof pitch slightly to appear more human-like.",
            };
            case "manualpest" -> new String[]{
                    "Manual Clean — Let the macro handle setup and movement, then stop before launching the pest cleaner script so you can clear pests manually.",
                    "Manual Ding — Play a local alert sound when Manual Clean reaches the point where you need to take over.",
                    "Custom Sound — Optional local audio file for the Manual Ding. Put your file in config/ihanuat/sounds/ and enter the full filename with extension, for example ding.wav. .wav is the recommended format. You can also paste a full absolute file path. If blank or invalid, the mod falls back to the system beep.",
                    "Manual Return — How long the macro should wait after the pest count reaches your target before returning to garden in Manual Clean mode.",
                    "Rewarp At — Return to garden when the live pest count is at or below this number. Set it to 0 for full clears, or 1+ if you intentionally want to leave pests behind.",
            };
            case "discomode" -> new String[]{
                    "Note: Must use a /plottp pest cage with Disco Destination.",
                    "Disco Destination — Bypass Taunahi's pest cleaner and have the mod hold right-click on the Pest Vacuum until pests are cleared.",
                    "Tp Before Wardrobe — Teleport to the infested plot before the wardrobe swap, so the post-tp delay overlaps with pests spawning. Forces /tptoplot (skips AOTV).",
                    "Disco Return — How long the macro should wait after the pest count reaches your target before returning to garden in Disco Mode.",
                    "Rewarp At — Return to garden when the live pest count is at or below this number. Set it to 0 for full clears, or 1+ if you intentionally want to leave pests behind.",
            };
            case "autovisitor" -> new String[]{
                    "Auto-Visitor — Automatically handle visitors that appear on your farm.",
                    "Threshold — Minimum number of visitors required before Auto Visitor activates.",
            };

            case "autosell" -> new String[]{
                    "Custom Autosell — Enable ihanuat's custom autosell routine to sell items from the Autosell Item List.",
                    "Autosell Item List — List of item names to sell automatically during the autosell sequence.",
            };
            case "wardrobeswap" -> new String[]{
                    "Auto Wardrobe (Pest) — Turn on/off changing wardrobe automatically when pests spawn.",
                    "Auto Wardrobe (Visitor) — Switch to the Visitor wardrobe slot when a visitor appears.",
                    "Armor Swap (Visitor) — Swap individual armor pieces for visitors instead of switching the full wardrobe slot. Use this if you want to keep your current wardrobe but change specific pieces.",
                    "Farming Slot — Which wardrobe slot to use for farming and pest clearing armor.",
                    "Pest Slot — Which wardrobe slot to use for pest spawning armor.",
                    "Visitor Slot — Which wardrobe slot to use when handling visitors.",
            };
            case "profitcalculator" -> new String[]{
                    "Session HUD — Display the profit earned during the current macro session.",
                    "Daily HUD — Display today's total profit on the HUD.",
                    "Lifetime HUD — Display total lifetime profit on the HUD.",
                    "HUD While Off — Keep the profit HUD visible even when the macro is not running.",
                    "Compact — Collapse all profit HUDs into a single condensed display.",
                    "Pet XP List — List of pets whose XP gains are tracked for profit calculation. Format: Pet Name, Max Level (100/200), Level 1 Price, Max Level Price, Rarity.",
            };
            case "general" -> new String[]{
                    "Show Macro HUD — Toggle the macro state/profit HUD overlay.",
                    "GUI Only in Garden — Only allow opening this GUI while in the Garden.",
                    "Enable PlotTP Rewarp — Auto re-warp when the player reaches the rewarp position.",
                    "Hold W Until Wall — Hold W after rewarp until hitting a wall.",
                    "Unfly Mode — How to land after flying (Double-tap Space or Sneak).",
                    "Farm Script — Which farming script the macro runs.",
                    "PlotTP Number — The plot number used for /plottp commands.",
                    "Capture Rewarp Pos — Save your current XYZ as the rewarp trigger point.",
                    "Auto-Resume After Rest — Automatically restart the macro after a Dynamic Rest break.",
                    "Auto-Recover Disconnect — Reconnect and resume if unexpectedly kicked.",
                    "Persist Session Timer — Keep the Dynamic Rest timer running across sessions.",
            };
            default -> new String[0];
        };
    }

    private static String[] getChineseHelperLines(String topic) {
        return switch (topic) {
            case "chatrules" -> new String[]{
                    "名称 - 显示在 Discord 提醒中的标签。",
                    "匹配文本 - 每条聊天消息中要扫描的精确文本。",
                    "包含/等于/开头/结尾 - 匹配文本的方式。",
                    "大小写 - 开启时区分大小写。",
                    "启用 - 仅在开启时生效。",
                    "任意匹配都会发送 Discord webhook 提醒。",
                    "在生活质量面板设置 webhook 地址。首个匹配优先。",
            };
            case "delays" -> new String[]{
                    "随机延迟 - 通过 getRandomizedDelay() 在每个定时动作上额外增加的随机毫秒数。作为大多数流程中的全局拟人抖动。",
                    "旋转 - 宏在开始耕作前转向作物所花费的时间。值越高越像真人，但会拖慢循环速度。",
                    "GUI 点击 - 菜单内每次模拟点击之间的间隔（衣柜、铁砧、George、垃圾丢弃界面）。适用于所有 GUI 交互。",
                    "装备切换 - 更换装备预设槽后暂停一段时间，让服务器有时间应用新装备后再执行下一步。",
                    "重生点冷却 - 在虫害清理期间，/setspawn 完成后等待多久再打开衣柜。默认 1000ms；网络稳定可降低，若衣柜交互偶发失败可提高。",
                    "鱼竿切换 - 切换到鱼竿后暂停一段时间。仅在启用自动鱼竿时生效（虫害 CD / 生成 / 返回农场触发）。",
                    "虫害聊天 - 在收到虫害聊天消息后等待多久，宏才会开始虫害清理流程。可用于等待聊天内容稳定。",
                    "书本合成 - 在铁砧合成书本时每次模拟点击之间的间隔。建议保持在约 150ms 以上以避免漏点。",
                    "自动出售点击 - 自动出售期间 Booster Cookie 菜单每次点击之间的间隔。建议高于 100ms 以避免服务器吞点击。",
                    "衣柜后切换 - 非 AOTV 路线下衣柜 GUI 关闭后的等待时间。如果宏在护甲尚未完全装备时就继续耕作，可以提高该值。",
                    "衣柜 AOTV - AOTV 路线下衣柜 GUI 关闭后的等待时间。通常较短，因为之后会立刻使用 AOTV；若切换时序有问题可调整。",
                    "AOTV 吸附 - 触发 AOTV 吸附扫描与真正传送之间的等待时间。过低可能导致传送前吸附不到虫害。",
            };
            case "dynamicrest" -> new String[]{
                    "耕作最小/最大值 - 在这个分钟范围内随机选择一次耕作时长。",
                    "休息最小/最大值 - 在这个分钟范围内随机选择一次休息（断开连接）时长。",
                    "显示今日总时长 - 在 HUD 中显示今天的有效耕作时间。",
                    "显示累计总时长 - 在 HUD 中显示总计有效耕作时间。",
                    "退出阈值 - 当独立退出计时器超过该小时数时停止宏（0 = 关闭）。",
                    "重置退出计时器 - 将独立退出计时器清零。",
                    "强制关闭 MC - 达到退出阈值时完全关闭 Minecraft。",
            };
            case "qol" -> new String[]{
                    "书本合成 - 在铁砧自动合成附魔书。",
                    "始终合成 - 即使宏没有在耕作时也进行合成。",
                    "书本阈值 - 达到该数量的书本后才会触发合成。",
                    "聊天清理 - 隐藏 Hypixel 中嘈杂的脚本/击杀消息。",
                    "自动丢弃垃圾 - 自动丢弃与垃圾列表匹配的物品。",
                    "垃圾列表 - 视为垃圾的物品名称片段列表。",
                    "垃圾阈值 - 背包中有这么多垃圾物品时进行丢弃。",
                    "垃圾 PlotTP - 丢弃垃圾前传送到的地块编号。",
                    "仓库管理 - 自动出售完成后自动执行 /pickupstash。",
                    "Discord 状态 - 定期向 webhook 发送宏状态。",
                    "Webhook 地址 - 用于状态和聊天提醒的 Discord webhook 地址。",
                    "Discord 间隔 - 多久（分钟）发送一次状态更新。",
                    "调试消息 - 在聊天中显示详细调试信息。",
                    "记录到文件 - 将调试消息写入游戏文件夹中的文件。",
            };
            case "autorod" -> new String[]{
                    "虫害 CD 时甩竿 - 当虫害冷却结束时自动使用鱼竿。",
                    "虫害生成时甩竿 - 当虫害出现在农场上时立即使用鱼竿。",
                    "返回农场时甩竿 - 清理虫害或处理访客后返回农场时使用鱼竿。",
            };
            case "equipmentgeorge" -> new String[]{
                    "自动装备 - 在虫害生成模式与虫害清理/耕作模式切换时自动更换装备预设槽。",
                    "自动 George 出售 - 通过 George 的 Abiphone 联系人自动出售宠物掉落物（需要先解锁 George 联系人）。",
                    "George 阈值 - 背包中达到该数量的宠物后才会触发出售给 George。",
            };
            case "equipmentswap" -> new String[]{
                    "自动装备 - 控制是否在虫害生成与虫害清理/耕作时切换装备。",
            };
            case "autogeorge" -> new String[]{
                    "自动 George 出售 - 是否将宠物掉落物出售给 George（需要 George 的 Abiphone 联系人）。",
                    "阈值 - 背包中出售给 George 所需的最低宠物数量。",
            };
            case "autopest" -> new String[]{
                    "启用 - 自动虫害处理的总开关。关闭后会暂停自动开始虫害流程，但不会影响宏的其他部分。",
                    "阈值 - 触发虫害清理前所需的虫害数量。",
                    "聊天触发 - 当聊天中出现虫害生成提示时自动开始虫害清理。",
                    "延后作物狂热 - 等待作物狂热结束后再清理虫害，以免损失加成。",
                    "AOTV 到屋顶 - 清理虫害前使用 Aspect of the Void 以太传送到屋顶。",
                    "AOTV 屋顶地块 - 可以 AOTV 上屋顶的地块编号列表，用逗号分隔。",
                    "AOTV 前破坏方块 - 在向屋顶使用 AOTV 以太传送前先破坏挡路的方块。",
                    "屋顶仰角 - 用 AOTV 瞄准屋顶时向上看的角度（度数）。",
                    "仰角拟人化 - 轻微随机化屋顶仰角，让行为更像真人。",
            };
            case "manualpest" -> new String[]{
                    "手动清理 - 让宏完成准备和移动，然后在启动虫害清理脚本前停下，让你手动清理虫害。",
                    "手动提示音 - 当手动清理进行到需要你接手时播放本地提示音。",
                    "自定义声音 - 手动提示音可选的本地音频文件。将文件放到 config/ihanuat/sounds/ 中，并填写完整文件名和扩展名，例如 ding.wav。推荐使用 .wav。也可以粘贴完整绝对路径。若为空或无效，则回退到系统蜂鸣声。",
                    "手动返回 - 在手动清理模式下，虫害数量达到目标后宏在返回花园前应等待多久。",
                    "回传于 - 当实时虫害数量小于或等于该值时返回花园。设为 0 表示完全清理；若你想故意留下一些虫害，可设为 1 或更高。",
            };
            case "discomode" -> new String[]{
                    "注意：迪斯科目的地必须配合 /plottp 虫害笼使用。",
                    "迪斯科目的地 - 绕过 Taunahi 的虫害清理，由模组按住右键操作虫害吸尘器直到虫害清理完毕。",
                    "传送先于换装 - 在换装前先传送到有虫害的地块，让传送后的延迟与虫害刷新重叠。会强制使用 /tptoplot（跳过 AOTV）。",
                    "迪斯科返回 - 在迪斯科模式下，虫害数量达到目标后宏在返回花园前应等待多久。",
                    "回传于 - 当实时虫害数量小于或等于该值时返回花园。设为 0 表示完全清理；若你想故意留下一些虫害，可设为 1 或更高。",
            };
            case "autovisitor" -> new String[]{
                    "自动访客 - 自动处理出现在农场中的访客。",
                    "阈值 - 启动自动访客所需的最少访客数量。",
            };
            case "autosell" -> new String[]{
                    "自定义自动出售 - 启用 ihanuat 的自定义自动出售流程，出售自动出售物品列表中的物品。",
                    "自动出售物品列表 - 在自动出售流程中自动卖出的物品名称列表。",
            };
            case "wardrobeswap" -> new String[]{
                    "自动衣柜（虫害） - 控制虫害出现时是否自动切换衣柜。",
                    "自动衣柜（访客） - 当访客出现时切换到访客衣柜槽位。",
                    "护甲切换（访客） - 处理访客时只更换单件护甲，而不是切换整个衣柜槽位。如果你想保留当前衣柜但更换特定部件，可使用此项。",
                    "耕作槽位 - 用于耕作和虫害清理护甲的衣柜槽位。",
                    "虫害槽位 - 用于虫害生成护甲的衣柜槽位。",
                    "访客槽位 - 处理访客时使用的衣柜槽位。",
            };
            case "profitcalculator" -> new String[]{
                    "会话 HUD - 显示当前宏会话中获得的利润。",
                    "每日 HUD - 在 HUD 上显示今日总利润。",
                    "总计 HUD - 在 HUD 上显示累计总利润。",
                    "关闭时显示 HUD - 即使宏未运行也保持利润 HUD 可见。",
                    "紧凑模式 - 将所有利润 HUD 合并为一个紧凑显示。",
                    "宠物经验列表 - 用于利润计算时追踪经验增长的宠物列表。格式：宠物名称，最高等级（100/200），1级价格，满级价格，稀有度。",
            };
            case "general" -> new String[]{
                    "显示宏 HUD - 切换宏状态/利润 HUD 叠加层。",
                    "仅在花园显示 GUI - 只允许在花园内打开该 GUI。",
                    "启用 PlotTP 回传 - 当玩家到达回传位置时自动重新回传。",
                    "按住 W 直到撞墙 - 回传后持续按住 W 直到撞墙。",
                    "取消飞行模式 - 飞行后如何落地（双击空格或潜行）。",
                    "农场脚本 - 宏运行的耕作脚本。",
                    "PlotTP 编号 - /plottp 命令所使用的地块编号。",
                    "记录回传坐标 - 将你当前的 XYZ 保存为回传触发点。",
                    "休息后自动继续 - 动态休息结束后自动重新启动宏。",
                    "断线后自动恢复 - 意外掉线后自动重连并继续。",
                    "保留会话计时器 - 让动态休息计时器在不同会话之间持续运行。",
            };
            default -> new String[0];
        };
    }

    private void renderHelperPanel(GuiGraphics g, int mx, int my, net.minecraft.client.gui.Font font) {
        if (helperTopic == null) return;

        String[] lines = getHelperLines(helperTopic);
        if (lines.length == 0) return;

        int lineH      = font.lineHeight + 3;
        int contentW   = HELPER_W - 14;
        int PAD_V      = 4;

        java.util.List<int[]> wrapped = wrapHelperLines(font, lines, contentW);
        int totalContentH = wrapped.size() * lineH;
        int visibleContentH = Math.min(totalContentH, HELPER_MAX_CONTENT_H);
        int panelH = HEADER_H + PAD_V + visibleContentH + PAD_V + 2;
        int maxScroll = Math.max(0, totalContentH - visibleContentH);
        helperScrollOffset = Math.max(0, Math.min(maxScroll, helperScrollOffset));

        if (helperX < 0) {
            helperX = width  - HELPER_W - 6;
            helperY = height - panelH - 20;
        }
        helperX = Math.max(0, Math.min(width  - HELPER_W, helperX));
        helperY = Math.max(0, Math.min(height - panelH, helperY));

        int hBg  = C_BG();
        int hHdr = C_HDR();
        int hAcc = MacroConfig.themeAccent;

        fillRoundRect(g, helperX - 2, helperY - 2, HELPER_W + 4, panelH + 4, 4, hAcc);
        fillRoundRect(g, helperX, helperY, HELPER_W, panelH, 3, hBg);

        fillRoundRect(g, helperX, helperY, HELPER_W, HEADER_H, PANEL_RADIUS, hHdr);
        g.fill(helperX, helperY + HEADER_H - PANEL_RADIUS, helperX + HELPER_W, helperY + HEADER_H, hHdr);
        g.fill(helperX + 2, helperY + HEADER_H - 1, helperX + HELPER_W - 2, helperY + HEADER_H, hAcc);
        String hTitle = "? Helper" + (helperTitle != null ? " — " + helperTitle : "");
        MacroConfig.drawStyledText(g, font, hTitle, helperX + 5, helperY + HEADER_H / 2 - 4,
                C_TXT());

        int closeX = helperX + HELPER_W - 14;
        boolean closeHov = mx >= closeX - 2 && mx <= closeX + 10 && my >= helperY && my <= helperY + HEADER_H;
        MacroConfig.drawStyledText(g, font, "x", closeX, helperY + HEADER_H / 2 - 4,
                closeHov ? 0xFFFF5555 : C_DIM());

        int contentX = helperX + 6;
        int contentY = helperY + HEADER_H + PAD_V;
        int clipBottom = contentY + visibleContentH;
        g.enableScissor(helperX + 2, contentY, helperX + HELPER_W - 4, clipBottom);

        int c1 = C_DIM();
        int c2 = C_ON2();
        int c3 = C_DIM();

        int cy = contentY - helperScrollOffset;
        for (int[] wl : wrapped) {
            drawWrappedLine(g, font, contentX, cy, contentW, wl, lines, c1, c2, c3);
            cy += lineH;
        }

        g.disableScissor();

        if (maxScroll > 0) {
            int sbX  = helperX + HELPER_W - 4;
            int sbY0 = contentY;
            int sbH  = visibleContentH;
            int thumbH = Math.max(10, sbH * sbH / totalContentH);
            int thumbY = sbY0 + (int)((sbH - thumbH) * ((float) helperScrollOffset / maxScroll));
            g.fill(sbX, sbY0, sbX + 3, sbY0 + sbH, C_OFF());
            g.fill(sbX, thumbY, sbX + 3, thumbY + thumbH, C_ACC());
        }
    }

    /**
     * Wraps helper lines to fit within maxW pixels.
     * Returns a list of int[] descriptors for each rendered row:
     *   { renderType, originalLineIndex, charStart, charEnd }
     * renderType: 0=plain, 1=label segment, 2=desc-first-line, 3=continuation
     */
    /**
     * Wraps helper lines into display rows.
     * int[] descriptor: { renderType, lineIndex, charStart, charEnd }
     *   type 0 = plain first row          -> c1 (normal)
     *   type 1 = two-tone first row       -> label in c2, separator+desc in c3
     *   type 2 = continuation of plain    -> c1, no indent
     *   type 3 = continuation of two-tone -> c3, no indent
     */
    private static java.util.List<int[]> wrapHelperLines(net.minecraft.client.gui.Font font, String[] lines, int maxW) {
        java.util.List<int[]> result = new java.util.ArrayList<>();
        for (int li = 0; li < lines.length; li++) {
            String raw = lines[li];
            int sepIdx = raw.indexOf(" — ");
            if (sepIdx < 0) sepIdx = raw.indexOf(" - ");

            if (sepIdx >= 0) {
                String label = raw.substring(0, sepIdx);
                String sep  = raw.substring(sepIdx, sepIdx + 3);
                String desc = raw.substring(sepIdx + 3);
                String full = label + sep + desc;

                if (font.width(full) <= maxW) {
                    result.add(new int[]{1, li, sepIdx, raw.length()});
                } else {
                    int prefixW   = font.width(label + sep);
                    int available = maxW - prefixW;
                    int cut = fitChars(font, desc, available);
                    result.add(new int[]{1, li, sepIdx, sepIdx + 3 + cut});
                    addContinuations(font, result, raw, sepIdx + 3 + cut, maxW, 3, li);
                }
            } else {
                int cut = fitChars(font, raw, maxW);
                if (cut >= raw.length()) {
                    result.add(new int[]{0, li, 0, raw.length()});
                } else {
                    result.add(new int[]{0, li, 0, cut});
                    addContinuations(font, result, raw, cut, maxW, 2, li);
                }
            }
        }
        return result;
    }

    /** Returns the number of chars from s that fit within maxW pixels, breaking at a word boundary. */
    private static int fitChars(net.minecraft.client.gui.Font font, String s, int maxW) {
        if (font.width(s) <= maxW) return s.length();
        int end = 0;
        while (end < s.length() && font.width(s.substring(0, end + 1)) <= maxW) end++;
        int wb = s.lastIndexOf(' ', end);
        return (wb > 0) ? wb : Math.max(1, end);
    }

    /** Appends continuation rows for the remainder of raw starting at pos. */
    private static void addContinuations(net.minecraft.client.gui.Font font,
                                         java.util.List<int[]> result, String raw, int startPos, int maxW, int contType, int li) {
        int pos = startPos;
        if (pos < raw.length() && raw.charAt(pos) == ' ') pos++;
        while (pos < raw.length()) {
            String rest = raw.substring(pos);
            int cut = fitChars(font, rest, maxW);
            result.add(new int[]{contType, li, pos, pos + cut});
            pos += cut;
            if (pos < raw.length() && raw.charAt(pos) == ' ') pos++;
        }
    }


    private static void drawWrappedLine(GuiGraphics g, net.minecraft.client.gui.Font font,
                                        int x, int y, int maxW, int[] wl, String[] lines, int c1, int c2, int c3) {
        int type = wl[0], li = wl[1], cs = wl[2], ce = wl[3];
        String raw = lines[li];
        if (type == 0) {
            MacroConfig.drawStyledText(g, font, raw.substring(cs, ce), x, y, c1);
        } else if (type == 1) {
            int sepIdx = raw.indexOf(" — ");
            if (sepIdx < 0) sepIdx = raw.indexOf(" - ");
            if (sepIdx < 0) {
                MacroConfig.drawStyledText(g, font, raw.substring(cs, ce), x, y, c2);
                return;
            }
            String label = raw.substring(0, sepIdx);
            String rest  = raw.substring(sepIdx, ce);
            MacroConfig.drawStyledText(g, font, label, x, y, c2);
            MacroConfig.drawStyledText(g, font, rest, x + font.width(label), y, c3);
        } else if (type == 2) {
            MacroConfig.drawStyledText(g, font, raw.substring(cs, ce), x, y, c1);
        } else {
            MacroConfig.drawStyledText(g, font, raw.substring(cs, ce), x, y, c3);
        }
    }


    void handleMouseClicked(int x, int y, int btn) {
        if (helperTopic != null && helperX >= 0) {
            int closeX = helperX + HELPER_W - 14;
            if (x >= closeX - 2 && x <= closeX + 10 && y >= helperY && y <= helperY + HEADER_H) {
                helperTopic = null;
                helperTitle = null;
                helperScrollOffset = 0;
                return;
            }
            if (x >= helperX && x <= helperX + HELPER_W && y >= helperY && y <= helperY + HEADER_H) {
                helperDragging = true;
                helperDragOffX = x - helperX;
                helperDragOffY = y - helperY;
                return;
            }
        }
        if (activeSubPanel != null) {
            if (activeSubPanel.contains(x, y)) {
                activeSubPanel.mouseClicked(x, y, btn, font);
                return;
            } else {
                activeSubPanel.commit();
                activeSubPanel = null;
                save();
                return;
            }
        }
        int searchW = 160;
        int searchX = width / 2 - searchW / 2;
        int searchY = 4;
        searchActive = x >= searchX && x <= searchX + searchW && y >= searchY && y <= searchY + SEARCH_H;
        if (searchActive) return;
        for (Panel panel : new ArrayList<>(panels)) {
            if (panel.headerContains(x, y)) {
                if (btn == 0 && hasHelperTopic(panel.title)) {
                    String hlbl = "?";
                    int hlblW = font.width(hlbl);
                    int hbx = panel.x + PANEL_W - hlblW - 4;
                    if (x >= hbx - 1 && x <= hbx + hlblW + 3 && y >= panel.y + 1 && y <= panel.y + HEADER_H - 1) {
                        String newTopic = getHelperTopicKey(panel.title);
                        if (newTopic != null && newTopic.equals(helperTopic)) {
                            helperTopic = null;
                            helperTitle = null;
                            helperScrollOffset = 0;
                        } else {
                            updateHelperForPanel(panel.title);
                        }
                        panels.remove(panel);
                        panels.add(0, panel);
                        return;
                    }
                }
                if (btn == 0) {
                    draggingPanel = panel;
                    dragOffX = x - panel.x;
                    dragOffY = y - panel.y;
                    dragMovedPanel = false;
                } else if (btn == 1) {
                    panel.collapsed = !panel.collapsed;
                    savePanelPositions();
                }
                panels.remove(panel);
                panels.add(0, panel);
                return;
            }
            if (panel.contains(x, y, searchQuery)) {
                panels.remove(panel);
                panels.add(0, panel);
                if (btn == 0 && panel.scrollbarContains(x, y, searchQuery)) {
                    scrollbarPanel = panel;
                    scrollbarDragStartY = y;
                    scrollbarDragStartOffset = panel.scrollOffset;
                    return;
                }
                Entry hit = panel.entryAt(x, y, searchQuery);
                if (hit != null) {
                    if (btn == 0 && hit instanceof SectionEntry se && se.helperTopic != null) {
                        int ey = panel.entryY(hit, searchQuery);
                        int hlblW = font.width("?");
                        int hbx = panel.x + PANEL_W - ENTRY_PAD - hlblW - 4;
                        if (x >= hbx - 1 && x <= hbx + hlblW + 3 && y >= ey && y < ey + ENTRY_H) {
                            if (se.helperTopic.equals(helperTopic)) { helperTopic = null; helperTitle = null; helperScrollOffset = 0; }
                            else { helperTopic = se.helperTopic; helperTitle = se.label; helperScrollOffset = 0; }
                            return;
                        }
                    }
                    if (btn == 0) {
                        if (hit instanceof SliderEntry se && !se.valueContains(x, y, panel.x + ENTRY_PAD, panel.entryY(hit, searchQuery), PANEL_W - ENTRY_PAD * 2, ENTRY_H, font)) {
                            draggingSlider = se;
                            draggingSliderPanel = panel;
                            se.onDrag(x, panel.x + ENTRY_PAD + 2, PANEL_W - ENTRY_PAD * 2 - 4);
                        } else {
                            SubPanel sp = hit.openSubPanel(x, y, width, height);
                            if (sp != null) activeSubPanel = sp;
                            else { hit.onClick(x, y); panel.scrollOffset = Math.min(panel.scrollOffset, panel.maxScroll(searchQuery)); }
                        }
                    } else if (btn == 1) {
                        if (hit instanceof SectionEntry se) {
                            se.collapsed = !se.collapsed;
                            panel.scrollOffset = Math.min(panel.scrollOffset, panel.maxScroll(searchQuery));
                        } else {
                            SubPanel sp = hit.openSubPanel(x, y, width, height);
                            if (sp != null) activeSubPanel = sp;
                        }
                    }
                }
                return;
            }
        }
    }

    void handleMouseReleased() {
        helperDragging = false;
        if (activeSubPanel instanceof ChatRulesSubPanel crs) {
            crs.stopDrag();
        }
        if (activeSubPanel instanceof ThemeLibrarySubPanel tls) {
            tls.stopDrag();
        }
        if (activeSubPanel instanceof ColorSubPanel cs) {
            cs.draggingSlider = -1;
        }
        if (draggingPanel != null || scrollbarPanel != null) savePanelPositions();
        draggingPanel = null;
        draggingSlider = null;
        draggingSliderPanel = null;
        scrollbarPanel = null;
    }

    void handleMouseDragged(int x, int y) {
        if (helperDragging) {
            helperX = Math.max(0, Math.min(width  - HELPER_W, x - helperDragOffX));
            helperY = Math.max(0, Math.min(height - 30,        y - helperDragOffY));
            return;
        }
        if (activeSubPanel instanceof ChatRulesSubPanel crs) {
            crs.drag(x, y);
            return;
        }
        if (activeSubPanel instanceof ThemeLibrarySubPanel tls) {
            tls.drag(x, y);
            return;
        }
        if (activeSubPanel instanceof ThemeLibrarySubPanel tls) {
            tls.drag(x, y);
            return;
        }
        if (activeSubPanel instanceof ColorSubPanel cs) {
            cs.drag(x);
            return;
        }
        if (draggingPanel != null) {
            draggingPanel.x = Math.max(0, Math.min(width - PANEL_W, x - dragOffX));
            draggingPanel.y = Math.max(0, Math.min(height - HEADER_H, y - dragOffY));
            dragMovedPanel = true;
        } else if (scrollbarPanel != null) {
            scrollbarPanel.dragScrollbar(y, scrollbarDragStartY, scrollbarDragStartOffset, searchQuery);
        } else if (draggingSlider != null && draggingSliderPanel != null) {
            draggingSlider.onDrag(x, draggingSliderPanel.x + ENTRY_PAD + 2, PANEL_W - ENTRY_PAD * 2 - 4);
        }
    }

    void handleMouseScrolled(int x, int y, double hScroll, double vScroll, boolean shift) {
        if (helperTopic != null && helperX >= 0 && x >= helperX && x <= helperX + HELPER_W
                && y >= helperY + HEADER_H && y <= helperY + HEADER_H + HELPER_MAX_CONTENT_H) {
            helperScrollOffset = Math.max(0, helperScrollOffset + (int)(-vScroll * 12));
            return;
        }
        if (shift) {
            int pan = (int) ((hScroll != 0 ? hScroll : vScroll) * 20);
            for (Panel p : panels) p.x += pan;
            return;
        }
        if (activeSubPanel != null && activeSubPanel.contains(x, y)) {
            activeSubPanel.scroll((int) -vScroll);
            return;
        }
        for (Panel panel : panels) {
            if (panel.contains(x, y, searchQuery)) {
                panel.scroll((int) -vScroll, searchQuery);
                return;
            }
        }
    }

    void handleKeyPressed(int key, int scan, int mods) {
        if (activeSubPanel != null) {
            if (key == 65 && (mods & GLFW.GLFW_MOD_CONTROL) != 0) {
                if (activeSubPanel instanceof StringInputSubPanel sip) { sip.tf.selectAll(); Minecraft.getInstance().keyboardHandler.setClipboard(sip.tf.value); return; }
                if (activeSubPanel instanceof IntInputSubPanel iip) { iip.tf.selectAll(); return; }
                if (activeSubPanel instanceof DoubleInputSubPanel dip) { dip.tf.selectAll(); return; }
                if (activeSubPanel instanceof ListInputSubPanel lip) { lip.selStart = 0; lip.selEnd = lip.value.length(); lip.cursorIndex = lip.value.length(); return; }
                if (activeSubPanel instanceof ThemeLibrarySubPanel tls) { (tls.focusedField == 0 ? tls.tfName : tls.tfCode).selectAll(); return; }
                if (activeSubPanel instanceof ChatRulesSubPanel crs) { (crs.focusedField == 0 ? crs.tfName : crs.tfMatch).selectAll(); return; }
            }
            if (activeSubPanel.keyPressed(key, scan, mods)) {
                if ((key == 257 || key == 335) && (mods & GLFW.GLFW_MOD_SHIFT) == 0 && !(activeSubPanel instanceof ListInputSubPanel)) {
                    activeSubPanel = null;
                    save();
                }
                if ((mods & GLFW.GLFW_MOD_SHIFT) != 0 && key >= 48 && key <= 57) {
                    suppressNextChar = true;
                }
                return;
            }
            if (key == 256) {
                activeSubPanel.commit();
                activeSubPanel = null;
                save();
                return;
            }
        }
        if ((mods & GLFW.GLFW_MOD_CONTROL) != 0 && key == GLFW.GLFW_KEY_F) {
            searchActive = true;
            return;
        }
        if (searchActive) {
            if (key == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                return;
            }
            if (key == GLFW.GLFW_KEY_V && (mods & GLFW.GLFW_MOD_CONTROL) != 0) {
                String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
                if (clip != null) searchQuery += clip;
                return;
            }
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                if (!searchQuery.isEmpty()) searchQuery = "";
                else searchActive = false;
                return;
            }
            if (key == 32) {
                searchQuery += ' ';
                return;
            }
            if ((mods & GLFW.GLFW_MOD_CONTROL) == 0) {
                String name = GLFW.glfwGetKeyName(key, scan);
                if (name != null && name.length() == 1) {
                    char c = name.charAt(0);
                    if ((mods & GLFW.GLFW_MOD_SHIFT) != 0) c = Character.toUpperCase(c);
                    searchQuery += c;
                    return;
                }
            }
            return;
        }
        if (key == 256) onClose();
    }

    void handleKeyReleased(int key) {
        if (key == 340 || key == 344) shiftHeld = false;
    }

    private boolean suppressNextChar = false;

    void handleCharTyped(char c, int mods) {
        if (suppressNextChar) { suppressNextChar = false; return; }
        if (activeSubPanel != null) {
            activeSubPanel.charTyped(c, mods);
            return;
        }
        if (searchActive && !Character.isISOControl(c)) searchQuery += c;
    }

    @Override
    public void onClose() {
        savePanelPositions();
        previewOriginalCode = null;
        activeLibraryIndex = -1;
        save();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static void save() {
        MacroConfig.save();
    }

    private void notifyMsg(String msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.displayClientMessage(Component.literal(msg), true);
    }

    private void savePanelPositions() {
        // CRITICAL: this array's order MUST match the panels.add(...) order in buildPanels().
        // The index of each title here = the index used to read/write clickGuiPanelPositions[].
        // If the two orders drift apart, panels get assigned the wrong saved positions and
        // shuffle a row each time the GUI opens (the "dancing tabs" bug).
        String[] order = {"General", "Delays", "Wardrobe Swap", "Auto Pest", "Manual Pest", "Profit Calculator", "Dynamic Rest", "QOL", "Theme", "Chat Rules", "Disco Mode"};
        int[][] positions = new int[order.length][3];
        for (int i = 0; i < order.length; i++)
            for (Panel p : panels)
                if (p.title.equals(order[i])) {
                    positions[i] = new int[]{p.x, p.y, p.collapsed ? 1 : 0};
                    break;
                }
        MacroConfig.clickGuiPanelPositions = positions;
        MacroConfig.save();
    }

    static void fillRoundRect(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        for (int row = 0; row < h; row++) {
            int ind = 0;
            if (row < r) {
                double d = r - row - 0.5;
                ind = (int) Math.ceil(r - 0.5 - Math.sqrt(r * r - d * d));
            } else if (row >= h - r) {
                double d = row - (h - r) + 0.5;
                ind = (int) Math.ceil(r - 0.5 - Math.sqrt(r * r - d * d));
            }
            g.fill(x + ind, y + row, x + w - ind, y + row + 1, color);
        }
    }


    static class Panel {
        String title;
        int x, y;
        boolean collapsed = true;
        int scrollOffset = 0;
        final List<Entry> entries = new ArrayList<>();

        Panel(String title, int x, int y) {
            this.title = title;
            this.x = x;
            this.y = y;
        }

        void add(Entry e) {
            entries.add(e);
        }

        List<Entry> filtered(String q) {
            List<Entry> visibleEntries = entries.stream().filter(this::isEntryVisible).collect(Collectors.toList());
            if (q == null || q.isBlank()) return visibleEntries;
            String query = q.toLowerCase();
            if (title.toLowerCase().contains(query)) return visibleEntries;
            return visibleEntries.stream().filter(e -> entryLabel(e).toLowerCase().contains(query)).collect(Collectors.toList());
        }

        String entryLabel(Entry e) {
            if (e instanceof VisibleEntry ve) return entryLabel(ve.entry);
            if (e instanceof ToggleEntry te) return te.label;
            if (e instanceof SliderEntry se) return se.label;
            if (e instanceof CycleEnumEntry<?> ce) return ce.label;
            if (e instanceof TextSettingEntry te) return te.label;
            if (e instanceof ListSettingEntry le) return le.label;
            if (e instanceof IntFieldEntry ie) return ie.label;
            if (e instanceof DoubleFieldEntry de) return de.label;
            if (e instanceof ButtonEntry be) return be.label;
            if (e instanceof ScriptSelectorEntry) return tr("Farm Script", "农场脚本");
            if (e instanceof ColorEntry ce) return ce.label;
            if (e instanceof ImportCodeEntry) return tr("Paste Theme Code", "粘贴主题代码");
            if (e instanceof SectionEntry se) return se.label;
            return "";
        }

        boolean isEntryVisible(Entry e) {
            return !(e instanceof VisibleEntry ve) || ve.isVisible();
        }

        List<Entry> flatEntries(String q) {
            List<Entry> result = new ArrayList<>();
            for (Entry e : filtered(q)) {
                result.add(e);
                if (e instanceof SectionEntry se && !se.collapsed) {
                    for (Entry child : se.children) {
                        if (isEntryVisible(child)) result.add(child);
                    }
                }
            }
            return result;
        }

        int contentHeight(String q) {
            return flatEntries(q).size() * (ENTRY_H + ENTRY_PAD) + ENTRY_PAD;
        }

        int visibleHeight() {
            return 200;
        }

        int maxScroll(String q) {
            return Math.max(0, contentHeight(q) - visibleHeight());
        }

        boolean headerContains(int mx, int my) {
            return mx >= x && mx <= x + PANEL_W && my >= y && my <= y + HEADER_H;
        }

        boolean contains(int mx, int my, String q) {
            if (collapsed || filtered(q).isEmpty()) return false;
            return mx >= x && mx <= x + PANEL_W && my >= y + HEADER_H && my <= y + HEADER_H + Math.min(contentHeight(q), visibleHeight());
        }

        Entry entryAt(int mx, int my, String q) {
            if (collapsed || filtered(q).isEmpty() || mx > x + PANEL_W - 4) return null;
            int ey = y + HEADER_H + ENTRY_PAD - scrollOffset;
            for (Entry e : flatEntries(q)) {
                if (my >= ey && my < ey + ENTRY_H && mx >= x && mx <= x + PANEL_W) return e;
                ey += ENTRY_H + ENTRY_PAD;
            }
            return null;
        }

        int entryY(Entry target, String q) {
            int ey = y + HEADER_H + ENTRY_PAD - scrollOffset;
            for (Entry e : flatEntries(q)) {
                if (e == target) return ey;
                ey += ENTRY_H + ENTRY_PAD;
            }
            return y + HEADER_H + ENTRY_PAD;
        }

        boolean scrollbarContains(int mx, int my, String q) {
            if (contentHeight(q) <= visibleHeight()) return false;
            int clipY = y + HEADER_H;
            int clipH = Math.min(contentHeight(q), visibleHeight());
            return mx >= x + PANEL_W - 4 && mx <= x + PANEL_W && my >= clipY && my <= clipY + clipH;
        }

        void dragScrollbar(int mouseY, int startMouseY, int startOffset, String q) {
            int content = contentHeight(q);
            int clipH = Math.min(content, visibleHeight());
            int thumbH = Math.max(10, clipH * clipH / content);
            int travel = Math.max(1, clipH - thumbH);
            int max = Math.max(1, maxScroll(q));
            int delta = mouseY - startMouseY;
            scrollOffset = Math.max(0, Math.min(maxScroll(q), startOffset + Math.round(delta * (max / (float) travel))));
        }

        void scroll(int dir, String q) {
            scrollOffset = Math.max(0, Math.min(maxScroll(q), scrollOffset + dir * 10));
        }

        void render(GuiGraphics g, int mx, int my, net.minecraft.client.gui.Font font, String q) {
            List<Entry> filtered = filtered(q);
            if (!q.isEmpty() && filtered.isEmpty() && !title.toLowerCase().contains(q.toLowerCase())) return;
            boolean effectiveCollapsed = collapsed || (!q.isEmpty() && filtered.isEmpty());
            int totalH = effectiveCollapsed ? HEADER_H : HEADER_H + Math.min(contentHeight(q), visibleHeight());
            int hdrCol = effectiveCollapsed ? C_HDR() : brighten(C_HDR(), 0x101010);
            boolean hdrTransparent = ((hdrCol >> 24) & 0xFF) < 255;
            if (hdrTransparent) {
                if (!effectiveCollapsed) g.fill(x, y + HEADER_H, x + PANEL_W, y + totalH, C_BG());
            } else {
                fillRoundRect(g, x, y, PANEL_W, totalH, PANEL_RADIUS, C_BG());
            }
            fillRoundRect(g, x, y, PANEL_W, HEADER_H, PANEL_RADIUS, hdrCol);
            if (!effectiveCollapsed && !hdrTransparent) g.fill(x, y + HEADER_H - PANEL_RADIUS, x + PANEL_W, y + HEADER_H, hdrCol);
            if (!hdrTransparent) g.fill(x + 2, y + HEADER_H - 1, x + PANEL_W - 2, y + HEADER_H, C_LINE());
            int arrowLX = x + 5;
            int arrowW = font.width("v") + 4;
            MacroConfig.drawStyledText(g, font, effectiveCollapsed ? ">" : "v", arrowLX, y + HEADER_H / 2 - 4, C_DIM());
            MacroConfig.drawStyledText(g, font, title, x + 5 + arrowW, y + HEADER_H / 2 - 4, !q.isEmpty() && title.toLowerCase().contains(q.toLowerCase()) ? C_ON2() : C_TXT());
            boolean hasHelper = hasHelperTopic(title);
            if (hasHelper) {
                String hlbl = "?";
                int hlblW = font.width(hlbl);
                int hbx = x + PANEL_W - hlblW - 4;
                boolean hbHov = mx >= hbx - 1 && mx <= hbx + hlblW + 3 && my >= y + 1 && my <= y + HEADER_H - 1;
                boolean hbActive = title.equals(helperTitle);
                if (hbHov || hbActive) fillRoundRect(g, hbx, y + 3, hlblW + 2, HEADER_H - 6, 2, hbActive ? C_ON() : C_HOVER());
                MacroConfig.drawStyledText(g, font, hlbl, hbx + 1, y + HEADER_H / 2 - 4, hbActive || hbHov ? C_TXT() : C_DIM());
            }
            if (effectiveCollapsed) return;

            int clipY = y + HEADER_H, clipH = Math.min(contentHeight(q), visibleHeight());
            g.enableScissor(x, clipY, x + PANEL_W, clipY + clipH);
            int ey = clipY + ENTRY_PAD - scrollOffset;
            java.util.Set<SectionEntry> openSections = new java.util.HashSet<>();
            for (Entry e : filtered) { if (e instanceof SectionEntry se && !se.collapsed) openSections.add(se); }
            for (Entry e : flatEntries(q)) {
                boolean isChild = openSections.stream().anyMatch(se -> se.children.contains(e));
                int indent = isChild ? 14 : 0;
                boolean hov = mx >= x && mx <= x + PANEL_W && my >= ey && my < ey + ENTRY_H;
                if (hov) g.fill(x + 1 + indent, ey, x + PANEL_W - 1, ey + ENTRY_H, C_HOVER());
                if (e instanceof SectionEntry se) {
                    se.updateHover(mx, my);
                    se.render(g, x + ENTRY_PAD, ey, PANEL_W - ENTRY_PAD * 2, ENTRY_H, hov, font);
                } else {
                    e.render(g, x + ENTRY_PAD + indent, ey, PANEL_W - ENTRY_PAD * 2 - indent, ENTRY_H, hov, font);
                }
                ey += ENTRY_H + ENTRY_PAD;
            }
            g.disableScissor();

        }
    }


    static class SectionEntry implements Entry {
        final String label;
        final String helperTopic;
        final List<Entry> children = new ArrayList<>();
        boolean collapsed = true;

        SectionEntry(String label, String helperTopic) {
            this.label = label;
            this.helperTopic = helperTopic;
        }

        void add(Entry e) { children.add(e); }

        List<Entry> visibleChildren() {
            return collapsed ? List.of() : children;
        }

        int lastMx = -1, lastMy = -1;
        void updateHover(int mx, int my) { lastMx = mx; lastMy = my; }
        boolean isHelpHovered(int x, int w, int y, int h, net.minecraft.client.gui.Font font) {
            if (helperTopic == null) return false;
            int hlblW = font.width("?");
            int hbx = x + w - hlblW - 4;
            return lastMx >= hbx - 1 && lastMx <= hbx + hlblW + 3 && lastMy >= y && lastMy < y + h;
        }

        @Override
        public void render(GuiGraphics g, int x, int y, int w, int h, boolean hov, net.minecraft.client.gui.Font font) {
            boolean hqActive = helperTopic != null && helperTopic.equals(ClickGui.helperTopic);
            boolean hqHov = isHelpHovered(x, w, y, h, font);
            fillRoundRect(g, x, y, w, h, 3, !collapsed || hov ? C_HDR() : C_BG());
            if (helperTopic != null) {
                String hlbl = "?";
                int hlblW = font.width(hlbl);
                int hbx = x + w - hlblW - 4;
                if (hqActive || hqHov) fillRoundRect(g, hbx, y + 3, hlblW + 2, h - 6, 2, hqActive ? C_ON() : C_HOVER());
                MacroConfig.drawStyledText(g, font, hlbl, hbx + 1, y + h / 2 - 4, hqActive || hqHov ? C_TXT() : C_DIM());
            }
            int secArrowW = font.width("v") + 4;
            MacroConfig.drawStyledText(g, font, collapsed ? ">" : "v", x + 2, y + h / 2 - 4, C_DIM());
            MacroConfig.drawStyledText(g, font, label, x + 2 + secArrowW, y + h / 2 - 4, hov ? C_TXT() : C_DIM());
        }

        @Override
        public void onClick(int mx, int my) {
            collapsed = !collapsed;
        }
    }

    interface Entry {
        void render(GuiGraphics g, int x, int y, int w, int h, boolean hov, net.minecraft.client.gui.Font font);

        void onClick(int mx, int my);

        default SubPanel openSubPanel(int mx, int my, int sw, int sh) {
            return null;
        }
    }

    static class VisibleEntry implements Entry {
        final Supplier<Boolean> visible;
        final Entry entry;

        VisibleEntry(Supplier<Boolean> visible, Entry entry) {
            this.visible = visible;
            this.entry = entry;
        }

        boolean isVisible() {
            return visible.get();
        }

        @Override
        public void render(GuiGraphics g, int x, int y, int w, int h, boolean hov, net.minecraft.client.gui.Font font) {
            entry.render(g, x, y, w, h, hov, font);
        }

        @Override
        public void onClick(int mx, int my) {
            entry.onClick(mx, my);
        }

        @Override
        public SubPanel openSubPanel(int mx, int my, int sw, int sh) {
            return entry.openSubPanel(mx, my, sw, sh);
        }
    }

    static class ToggleEntry implements Entry {
        final String label;
        final Supplier<Boolean> getter;
        final Consumer<Boolean> setter;

        ToggleEntry(String l, Supplier<Boolean> g, Consumer<Boolean> s) {
            label = l;
            getter = g;
            setter = s;
        }

        @Override
        public void render(GuiGraphics g, int x, int y, int w, int h, boolean hov, net.minecraft.client.gui.Font font) {
            boolean on = getter.get();
            int mid = y + h / 2;
            int dw = 12, dh = 7, dx = x + w - dw - 2, dy = mid - dh / 2;
            fillRoundRect(g, dx, dy, dw, dh, 3, on ? C_ON() : C_OFF());
            if (on) fillRoundRect(g, dx + 5, dy + 1, 5, 5, 2, C_ON2());
            MacroConfig.drawStyledText(g, font, label, x + 2, mid - 4, hov ? C_TXT() : C_DIM());
        }

        @Override
        public void onClick(int mx, int my) {
            setter.accept(!getter.get());
        }
    }

    static class SliderEntry implements Entry {
        final String label, fieldName;
        final int min, max;
        final Supplier<Integer> getter;
        final Consumer<Integer> setter;
        final String unit;

        SliderEntry(String l, String field, int mn, int mx, Supplier<Integer> g, Consumer<Integer> s, String u) {
            label = l; fieldName = field; min = mn; max = mx; getter = g; setter = s; unit = u;
        }

        @Override
        public void render(GuiGraphics g, int x, int y, int w, int h, boolean hov, net.minecraft.client.gui.Font font) {
            int val = getter.get();
            String vs = val + unit;
            int vw = font.width(vs);
            MacroConfig.drawStyledText(g, font, label, x + 2, y + 2, hov ? C_TXT() : C_DIM());
            MacroConfig.drawStyledText(g, font, vs, x + w - vw - 2, y + 2, C_ON2());
            int by = y + h - 5, bh = 3;
            fillRoundRect(g, x + 2, by, w - 4, bh, 1, C_SBGR());
            float frac = (float) (val - min) / (max - min);
            int fw = (int) (frac * (w - 4));
            if (fw > 0) fillRoundRect(g, x + 2, by, fw, bh, 1, C_SFILL());
            int kx = x + 2 + fw - 2;
            g.fill(Math.max(x + 2, kx), by - 1, Math.max(x + 2, kx) + 4, by + bh + 1, C_SKNOB());
        }

        boolean valueContains(int mx, int my, int x, int y, int w, int h, net.minecraft.client.gui.Font font) {
            String vs = getter.get() + unit;
            int vw = font.width(vs);
            return mx >= x + w - vw - 2 && mx <= x + w - 2 && my >= y && my <= y + h;
        }

        void onDrag(int mx, int barX, int barW) {
            float f = Math.max(0f, Math.min(1f, (float) (mx - barX) / barW));
            setter.accept(min + Math.round(f * (max - min)));
        }

        @Override
        public void onClick(int mx, int my) {
        }

        @Override
        public SubPanel openSubPanel(int mx, int my, int sw, int sh) {
            return new IntInputSubPanel(mx, my, sw, sh, label, getter.get(), MacroConfig.getDefaultInt(fieldName), min, max, v -> {
                setter.accept(v);
                save();
            });
        }
    }

    static class CycleEnumEntry<E extends Enum<E>> implements Entry {
        final String label;
        final E[] values;
        final Supplier<E> getter;
        final Consumer<E> setter;

        CycleEnumEntry(String l, E[] vs, Supplier<E> g, Consumer<E> s) {
            label = l;
            values = vs;
            getter = g;
            setter = s;
        }

        @Override
        public void render(GuiGraphics g, int x, int y, int w, int h, boolean hov, net.minecraft.client.gui.Font font) {
            String val = getter.get().toString();
            int vw = font.width(val);
            int mid = y + h / 2 - 4;
            MacroConfig.drawStyledText(g, font, label, x + 2, mid, hov ? C_TXT() : C_DIM());
            MacroConfig.drawStyledText(g, font, val, x + w - vw - 2, mid, C_ON2());
        }

        @Override
        public void onClick(int mx, int my) {
            E cur = getter.get();
            setter.accept(values[(cur.ordinal() + 1) % values.length]);
        }
    }

    static class TextSettingEntry implements Entry {
        final String label, fieldName;
        final Supplier<String> getter;
        final Consumer<String> setter;
        final String placeholder;

        TextSettingEntry(String l, String field, Supplier<String> g, Consumer<String> s) {
            this(l, field, g, s, "");
        }

        TextSettingEntry(String l, String field, Supplier<String> g, Consumer<String> s, String p) {
            label = l; fieldName = field; getter = g; setter = s; placeholder = p;
        }

        @Override
        public void render(GuiGraphics g, int x, int y, int w, int h, boolean hov, net.minecraft.client.gui.Font font) {
            String val = getter.get();
            if (val.length() > 11) val = val.substring(0, 9) + "..";
            int vw = font.width(val);
            int mid = y + h / 2 - 4;
            MacroConfig.drawStyledText(g, font, label, x + 2, mid, hov ? C_TXT() : C_DIM());
            MacroConfig.drawStyledText(g, font, val, x + w - vw - 6, mid, C_DIM());
        }

        @Override
        public void onClick(int mx, int my) {
        }

        @Override
        public SubPanel openSubPanel(int mx, int my, int sw, int sh) {
            return new StringInputSubPanel(mx, my, sw, sh, label, getter.get(), MacroConfig.getDefaultString(fieldName), setter, placeholder);
        }
    }

    static class ListSettingEntry implements Entry {
        final String label, fieldName;
        final Supplier<List<String>> getter;
        final Consumer<List<String>> setter;
        final String[] hints;

        ListSettingEntry(String l, String field, Supplier<List<String>> g, Consumer<List<String>> s, String... hints) {
            label = l; fieldName = field;
            getter = g;
            setter = s;
            this.hints = hints == null ? new String[0] : hints;
        }

        @Override
        public void render(GuiGraphics g, int x, int y, int w, int h, boolean hov, net.minecraft.client.gui.Font font) {
            List<String> values = getter.get();
            String summary;
            if (values == null || values.isEmpty()) summary = "(empty)";
            else if (values.size() == 1) summary = values.get(0);
            else summary = values.get(0) + " +" + (values.size() - 1);
            if (summary.length() > 11) summary = summary.substring(0, 9) + "..";
            int vw = font.width(summary);
            int mid = y + h / 2 - 4;
            MacroConfig.drawStyledText(g, font, label, x + 2, mid, hov ? C_TXT() : C_DIM());
            MacroConfig.drawStyledText(g, font, summary, x + w - vw - 6, mid, C_DIM());
        }

        @Override
        public void onClick(int mx, int my) {
        }

        @Override
        public SubPanel openSubPanel(int mx, int my, int sw, int sh) {
            return new ListInputSubPanel(mx, my, sw, sh, label, MacroConfig.getDefaultList(fieldName), getter.get(), setter, hints);
        }
    }

    static class IntFieldEntry implements Entry {
        final String label, fieldName;
        final Supplier<Integer> getter;
        final Consumer<Integer> setter;
        final String unit;

        IntFieldEntry(String l, String field, Supplier<Integer> g, Consumer<Integer> s, String u) {
            label = l; fieldName = field; getter = g; setter = s; unit = u;
        }

        @Override
        public void render(GuiGraphics g, int x, int y, int w, int h, boolean hov, net.minecraft.client.gui.Font font) {
            String val = getter.get() + " " + unit;
            int vw = font.width(val);
            int mid = y + h / 2 - 4;
            MacroConfig.drawStyledText(g, font, label, x + 2, mid, hov ? C_TXT() : C_DIM());
            MacroConfig.drawStyledText(g, font, val, x + w - vw - 2, mid, C_ON2());
        }

        @Override
        public void onClick(int mx, int my) {
        }

        @Override
        public SubPanel openSubPanel(int mx, int my, int sw, int sh) {
            return new IntInputSubPanel(mx, my, sw, sh, label, getter.get(), MacroConfig.getDefaultInt(fieldName), Integer.MIN_VALUE, Integer.MAX_VALUE, v -> {
                setter.accept(v);
                save();
            });
        }
    }

    static class DoubleFieldEntry implements Entry {
        final String label, fieldName;
        final Supplier<Double> getter;
        final Consumer<Double> setter;
        final String unit;

        DoubleFieldEntry(String l, String field, Supplier<Double> g, Consumer<Double> s, String u) {
            label = l; fieldName = field; getter = g; setter = s; unit = u;
        }

        @Override
        public void render(GuiGraphics g, int x, int y, int w, int h, boolean hov, net.minecraft.client.gui.Font font) {
            String val = String.format("%.2f %s", getter.get(), unit);
            int vw = font.width(val);
            int mid = y + h / 2 - 4;
            MacroConfig.drawStyledText(g, font, label, x + 2, mid, hov ? C_TXT() : C_DIM());
            MacroConfig.drawStyledText(g, font, val, x + w - vw - 2, mid, C_ON2());
        }

        @Override
        public void onClick(int mx, int my) {
        }

        @Override
        public SubPanel openSubPanel(int mx, int my, int sw, int sh) {
            return new DoubleInputSubPanel(mx, my, sw, sh, label, getter.get(), MacroConfig.getDefaultDouble(fieldName), v -> {
                setter.accept(v);
                save();
            });
        }
    }

    static class ButtonEntry implements Entry {
        final String label;
        final Runnable action;

        ButtonEntry(String l, Runnable a) {
            label = l;
            action = a;
        }

        @Override
        public void render(GuiGraphics g, int x, int y, int w, int h, boolean hov, net.minecraft.client.gui.Font font) {
            if (hov) fillRoundRect(g, x + 2, y + 2, w - 4, h - 4, 3, C_HOVER());
            int tw = font.width(label);
            MacroConfig.drawStyledText(g, font, label, x + (w - tw) / 2, y + h / 2 - 4, hov ? C_TXT() : C_DIM());
        }

        @Override
        public void onClick(int mx, int my) {
            action.run();
        }
    }

    static class ColorEntry implements Entry {
        final String label;
        final Supplier<Integer> getter;
        final Consumer<Integer> setter;
        final boolean supportsAlpha;

        ColorEntry(String l, Supplier<Integer> g, Consumer<Integer> s) {
            this(l, g, s, true);
        }
        ColorEntry(String l, Supplier<Integer> g, Consumer<Integer> s, boolean supportsAlpha) {
            label = l; getter = g; setter = s; this.supportsAlpha = supportsAlpha;
        }

        @Override
        public void render(GuiGraphics g, int x, int y, int w, int h, boolean hov, net.minecraft.client.gui.Font font) {
            int col = getter.get();
            int mid = y + h / 2 - 4;
            MacroConfig.drawStyledText(g, font, label, x + 2, mid, hov ? C_TXT() : C_DIM());
            int sw = 16, sx = x + w - sw - 2, sy = y + 2;
            g.fill(sx - 1, sy - 1, sx + sw + 1, sy + h - 2, 0xFF555555);
            g.fill(sx, sy, sx + sw, sy + h - 4, col | 0xFF000000);
            String hex = String.format("#%06X", col & 0xFFFFFF);
            int hw = font.width(hex);
            MacroConfig.drawStyledText(g, font, hex, x + w - sw - hw - 6, mid, C_DIM());
        }

        @Override
        public void onClick(int mx, int my) {
        }

        @Override
        public SubPanel openSubPanel(int mx, int my, int sw, int sh) {
            return new ColorSubPanel(mx, my, sw, sh, label, getter.get(), setter, supportsAlpha);
        }
    }

    static class ImportCodeEntry implements Entry {
        @Override
        public void render(GuiGraphics g, int x, int y, int w, int h, boolean hov, net.minecraft.client.gui.Font font) {
            if (hov) fillRoundRect(g, x + 2, y + 2, w - 4, h - 4, 3, C_HOVER());
            String lbl = tr("Paste Theme Code", "粘贴主题代码");
            int tw = font.width(lbl);
            MacroConfig.drawStyledText(g, font, lbl, x + (w - tw) / 2, y + h / 2 - 4, hov ? C_TXT() : C_DIM());
        }

        @Override
        public void onClick(int mx, int my) {
        }

        @Override
        public SubPanel openSubPanel(int mx, int my, int sw, int sh) {
            return new StringInputSubPanel(mx, my, sw, sh, tr("Paste Theme Code", "粘贴主题代码"), "", "", v -> {
                if (applyThemeCode(v)) {
                    Minecraft.getInstance().player.displayClientMessage(Component.literal(tr("Theme applied!", "主题已应用！")), true);
                } else {
                    Minecraft.getInstance().player.displayClientMessage(Component.literal(tr("Invalid theme code!", "主题代码无效！")), true);
                }
            });
        }
    }


    static class ThemeLibraryEntry implements Entry {
        @Override
        public void render(GuiGraphics g, int x, int y, int w, int h, boolean hov, net.minecraft.client.gui.Font font) {
            if (hov) fillRoundRect(g, x + 2, y + 2, w - 4, h - 4, 3, C_HOVER());
            int count = MacroConfig.savedThemes.size();
            String lbl = tr("Theme Library", "主题库") + " (" + count + ")";
            int tw = font.width(lbl);
            MacroConfig.drawStyledText(g, font, lbl, x + (w - tw) / 2, y + h / 2 - 4, hov ? C_TXT() : C_DIM());
        }

        @Override
        public void onClick(int mx, int my) {}

        @Override
        public SubPanel openSubPanel(int mx, int my, int sw, int sh) {
            return new ThemeLibrarySubPanel(mx, my, sw, sh);
        }
    }

    static class ThemeLibrarySubPanel implements SubPanel {
        private static final int W        = 340;
        private static final int ROW_H    = 18;
        private static final int PAD      = 4;
        private static final int HDR_H    = 16;
        private static final int LIST_MAX = 160;
        private static final int BTN_W    = 14;
        private static final int BTN_H    = 10;

        private final int screenW, screenH;
        private int panelX, panelY;
        private int scrollOffset = 0;
        private boolean deleteMode = false;
        private final java.util.Set<Integer> checkedForDelete = new java.util.HashSet<>();

        private boolean dragging = false;
        private int dragOffX, dragOffY;

        private boolean addingNew   = false;
        private boolean editingMode = false;
        private int     editingIndex = -1;
        final TextField tfName = new TextField("");
        final TextField tfCode = new TextField("");
        int focusedField = 0;

        ThemeLibrarySubPanel(int mx, int my, int sw, int sh) {
            this.screenW = sw; this.screenH = sh;
            int h = calcTotalHeight();
            this.panelX = Math.max(4, Math.min(mx, sw - W - 4));
            this.panelY = Math.max(4, Math.min(my, sh - h - 4));
        }

        private boolean isFormOpen() { return addingNew || editingMode; }

        private int listContentH() {
            return Math.max(PAD, MacroConfig.savedThemes.size() * (ROW_H + PAD) + PAD);
        }
        private int listVisibleH() { return Math.min(listContentH(), LIST_MAX); }
        private int maxScroll()    { return Math.max(0, listContentH() - listVisibleH()); }
        private int addFormH()     { return PAD + ROW_H + PAD + ROW_H + PAD + ROW_H + PAD + ROW_H + PAD; }
        private int calcTotalHeight() {
            int h = HDR_H + PAD + listVisibleH() + PAD;
            if (isFormOpen()) h += addFormH() + PAD;
            return Math.min(h, screenH - 8);
        }
        private int listY() { return panelY + HDR_H + PAD; }
        private int formY() { return listY() + listVisibleH() + PAD; }

        private int btnCenterY()  { return panelY + HDR_H / 2 - BTN_H / 2; }
        private int addBtnX()     { return panelX + W - PAD - BTN_W; }
        private int delBtnX()     { return addBtnX() - BTN_W - 3; }
        private int confirmBtnX() { return delBtnX() - 50 - 3; }
        private int saveBtnX(net.minecraft.client.gui.Font font) { return delBtnX() - font.width("save") - 8; }

        private static String stripSpaces(String s) { return s.replace(" ", ""); }

        @Override
        public void render(GuiGraphics g, int mx, int my, net.minecraft.client.gui.Font font) {
            int totalH = calcTotalHeight();
            panelY = Math.max(4, Math.min(panelY, screenH - totalH - 4));

            fillRoundRect(g, panelX - 2, panelY - 2, W + 4, totalH + 4, 4, C_SPBD());
            fillRoundRect(g, panelX, panelY, W, totalH, 3, C_SPBG());

            fillRoundRect(g, panelX, panelY, W, HDR_H, PANEL_RADIUS, C_HDR());
            g.fill(panelX, panelY + HDR_H - PANEL_RADIUS, panelX + W, panelY + HDR_H, C_HDR());
            g.fill(panelX + 2, panelY + HDR_H - 1, panelX + W - 2, panelY + HDR_H, C_LINE());
            MacroConfig.drawStyledText(g, font, tr("Theme Library", "主题库"), panelX + 5, panelY + HDR_H / 2 - 4, C_TXT());

            int bcy = btnCenterY();
            int abx = addBtnX(), dbx = delBtnX();
            boolean addHov = mx >= abx && mx <= abx + BTN_W && my >= bcy && my <= bcy + BTN_H;
            boolean delHov = mx >= dbx && mx <= dbx + BTN_W && my >= bcy && my <= bcy + BTN_H;

            if (addingNew || addHov) fillRoundRect(g, abx, bcy, BTN_W, BTN_H, 2, addingNew ? C_ON() : C_HOVER());
            MacroConfig.drawStyledText(g, font, "+", abx + (BTN_W - font.width("+")) / 2, bcy + BTN_H / 2 - 4, addingNew || addHov ? C_TXT() : C_DIM());

            if (deleteMode || delHov) fillRoundRect(g, dbx, bcy, BTN_W, BTN_H, 2, deleteMode ? C_ON() : C_HOVER());
            MacroConfig.drawStyledText(g, font, "-", dbx + (BTN_W - font.width("-")) / 2, bcy + BTN_H / 2 - 4, deleteMode || delHov ? C_TXT() : C_DIM());

            if (deleteMode && !checkedForDelete.isEmpty()) {
                int cbw = font.width("confirm");
                int cbx = confirmBtnX();
                boolean confHov = mx >= cbx - 2 && mx <= cbx + cbw + 2 && my >= bcy && my <= bcy + BTN_H;
                MacroConfig.drawStyledText(g, font, "confirm", cbx, bcy + BTN_H / 2 - 4, confHov ? 0xFFFF5555 : C_DIM());
            }

            {
                boolean hasPendingChanges = hasPendingThemeChanges();
                if (hasPendingChanges) {
                    int sbx = saveBtnX(font);
                    int svw = font.width("save");
                    boolean saveHov = mx >= sbx - 2 && mx <= sbx + svw + 2 && my >= bcy && my <= bcy + BTN_H;
                    fillRoundRect(g, sbx - 2, bcy, svw + 4, BTN_H, 2, saveHov ? C_BTN() : C_HOVER());
                    MacroConfig.drawStyledText(g, font, "save", sbx, bcy + BTN_H / 2 - 4, C_TXT());
                }
            }

            int lY = listY(), lH = listVisibleH();
            g.enableScissor(panelX, lY, panelX + W, lY + lH);
            int ey = lY - scrollOffset;
            java.util.List<String> themes = MacroConfig.savedThemes;
            for (int i = 0; i < themes.size(); i++) {
                String[] parts = themes.get(i).split("\\|", 2);
                String name = parts.length > 0 ? parts[0] : "?";
                boolean checked = checkedForDelete.contains(i);

                if (deleteMode) {
                    int chkX = panelX + PAD + 2;
                    int chkS = ROW_H - 4;
                    boolean chkHov = mx >= chkX && mx <= chkX + chkS && my >= ey + 2 && my < ey + ROW_H - 2;
                    fillRoundRect(g, chkX, ey + 2, chkS, chkS, 2, checked ? C_ON() : (chkHov ? C_HOVER() : C_OFF()));
                    MacroConfig.drawStyledText(g, font, name, panelX + PAD + chkS + 6, ey + (ROW_H - 8) / 2, checked ? C_DIM() : C_TXT());
                } else {
                    int copyX = panelX + W - PAD - 34;
                    int editX = copyX - 30 - 3;
                    boolean rowHov  = mx >= panelX + PAD && mx < editX && my >= ey && my < ey + ROW_H;
                    boolean editHov = mx >= editX && mx <= editX + 28 && my >= ey + 2 && my < ey + ROW_H - 2;
                    boolean copyHov = mx >= copyX && mx <= copyX + 32 && my >= ey + 2 && my < ey + ROW_H - 2;
                    if (activeLibraryIndex == i) fillRoundRect(g, panelX + PAD, ey, W - PAD * 2, ROW_H, 2, C_ON());
                    else if (rowHov) fillRoundRect(g, panelX + PAD, ey, editX - panelX - PAD, ROW_H, 2, C_HOVER());
                    if (editHov || (editingMode && editingIndex == i)) fillRoundRect(g, editX, ey + 2, 28, ROW_H - 4, 2, (editingMode && editingIndex == i) ? C_ON() : C_HOVER());
                    if (copyHov) fillRoundRect(g, copyX, ey + 2, 32, ROW_H - 4, 2, C_HOVER());
                    MacroConfig.drawStyledText(g, font, "edit", editX + (28 - font.width("edit")) / 2, ey + (ROW_H - 8) / 2, editHov || (editingMode && editingIndex == i) ? C_TXT() : C_DIM());
                    MacroConfig.drawStyledText(g, font, "copy", copyX + (32 - font.width("copy")) / 2, ey + (ROW_H - 8) / 2, copyHov ? C_TXT() : C_DIM());
                    MacroConfig.drawStyledText(g, font, name, panelX + PAD + 4, ey + (ROW_H - 8) / 2, C_TXT());
                }
                ey += ROW_H + PAD;
            }
            g.disableScissor();

            if (maxScroll() > 0) {
                int bh = Math.max(10, lH * lH / listContentH());
                int by = lY + (int)((lH - bh) * ((float) scrollOffset / maxScroll()));
                g.fill(panelX + W - 4, lY, panelX + W - 2, lY + lH, C_OFF());
                g.fill(panelX + W - 4, by, panelX + W - 2, by + bh, C_ACC());
            }

            if (isFormOpen()) {
                int fy  = formY();
                int ep  = addFormH();
                int fx  = panelX + PAD + 4;
                int fw  = W - PAD * 2 - 8;
                int lw  = font.width("Name:");
                int cy  = fy + PAD;

                String formTitle = editingMode ? tr("Edit Theme", "编辑主题") : tr("Add Theme", "添加主题");
                MacroConfig.drawStyledText(g, font, formTitle, fx, cy + (ROW_H - 8) / 2, C_TXT());
                cy += ROW_H + PAD;

                MacroConfig.drawStyledText(g, font, "Name:", fx, cy + (ROW_H - 8) / 2, C_DIM());
                tfName.render(g, font, fx + lw + 4, cy, fw - lw - 4, ROW_H, focusedField == 0);
                cy += ROW_H + PAD;

                int _codeLabelY = cy + (ROW_H - 8) / 2;
                MacroConfig.drawStyledText(g, font, "Code:", fx, _codeLabelY, C_DIM());
                if (tfCode.value.isBlank()) {
                    String _hint = "*optional";
                    int _hw = font.width(_hint);
                    int _hx = fx + (font.width("Code:") - _hw) / 2;
                    int _hy = _codeLabelY + font.lineHeight + 3;
                    MacroConfig.drawStyledText(g, font, _hint, _hx, _hy, darken(C_DIM(), 0x202020));
                }
                tfCode.render(g, font, fx + lw + 4, cy, fw - lw - 4, ROW_H, focusedField == 1);
                cy += ROW_H + PAD;


                int saveLbl = font.width("Save");
                int saveW   = saveLbl + 16;
                int saveX   = fx + (fw - saveW) / 2;
                boolean saveHov = mx >= saveX && mx <= saveX + saveW && my >= cy && my < cy + ROW_H;
                fillRoundRect(g, saveX, cy, saveW, ROW_H, 2, saveHov ? C_BTN() : brighten(C_ON(), 0x111111));
                MacroConfig.drawStyledText(g, font, "Save", saveX + (saveW - saveLbl) / 2, cy + (ROW_H - 8) / 2, C_TXT());
            }
        }

        @Override
        public boolean contains(int mx, int my) {
            int h = calcTotalHeight();
            return mx >= panelX - 2 && mx <= panelX + W + 2
                    && my >= panelY - 2 && my <= panelY + h + 2;
        }

        @Override
        public boolean mouseClicked(int mx, int my, int btn, net.minecraft.client.gui.Font font) {
            if (mx >= panelX && mx <= panelX + W && my >= panelY && my <= panelY + HDR_H) {
                int bcy = btnCenterY();
                int abx = addBtnX(), dbx = delBtnX();
                if (mx >= abx && mx <= abx + BTN_W && my >= bcy && my <= bcy + BTN_H) {
                    addingNew = !addingNew;
                    if (addingNew) {
                        editingMode = false; editingIndex = -1;
                        deleteMode = false; checkedForDelete.clear();
                        tfName.value = ""; tfName.cursor = 0; tfName.clearSelection(); tfCode.value = ""; tfCode.cursor = 0; tfCode.clearSelection(); focusedField = 0;
                    }
                    return true;
                }
                if (mx >= dbx && mx <= dbx + BTN_W && my >= bcy && my <= bcy + BTN_H) {
                    deleteMode = !deleteMode;
                    if (deleteMode) { addingNew = false; editingMode = false; editingIndex = -1; checkedForDelete.clear(); }
                    return true;
                }
                if (deleteMode && !checkedForDelete.isEmpty()) {
                    int cbx = confirmBtnX();
                    int cbw = net.minecraft.client.Minecraft.getInstance().font.width("confirm");
                    if (mx >= cbx - 2 && mx <= cbx + cbw + 2 && my >= bcy && my <= bcy + BTN_H) {
                        java.util.List<Integer> sorted = new java.util.ArrayList<>(checkedForDelete);
                        sorted.sort(java.util.Collections.reverseOrder());
                        for (int idx : sorted) if (idx < MacroConfig.savedThemes.size()) MacroConfig.savedThemes.remove(idx);
                        if (checkedForDelete.contains(activeLibraryIndex)) { activeLibraryIndex = -1; previewOriginalCode = null; }
                        else {
                            long shifted = checkedForDelete.stream().filter(idx -> idx < activeLibraryIndex).count();
                            activeLibraryIndex = (int)(activeLibraryIndex - shifted);
                        }
                        MacroConfig.save();
                        checkedForDelete.clear();
                        deleteMode = false;
                        return true;
                    }
                }
                {
                    net.minecraft.client.gui.Font f = Minecraft.getInstance().font;
                    int sbx = saveBtnX(f);
                    int svw = f.width("save");
                    if (mx >= sbx - 2 && mx <= sbx + svw + 2 && my >= bcy && my <= bcy + BTN_H && hasPendingThemeChanges()) {
                        if (activeLibraryIndex < MacroConfig.savedThemes.size()) {
                            String[] parts = MacroConfig.savedThemes.get(activeLibraryIndex).split("\\|", 2);
                            String name = parts.length > 0 ? parts[0] : "";
                            String code = encodeTheme();
                            MacroConfig.savedThemes.set(activeLibraryIndex, name + "|" + code);
                            previewOriginalCode = null;
                            if (editingMode && editingIndex == activeLibraryIndex) { tfCode.value = code; tfCode.cursor = code.length(); tfCode.clearSelection(); }
                            MacroConfig.save();
                        }
                        return true;
                    }
                }
                dragging = true;
                dragOffX = mx - panelX;
                dragOffY = my - panelY;
                return true;
            }

            int lY = listY(), lH = listVisibleH();
            if (mx >= panelX + PAD && mx <= panelX + W - PAD && my >= lY && my < lY + lH) {
                int ey = lY - scrollOffset;
                java.util.List<String> themes = MacroConfig.savedThemes;
                for (int i = 0; i < themes.size(); i++) {
                    if (my >= ey && my < ey + ROW_H) {
                        if (deleteMode) {
                            int chkX = panelX + PAD + 2;
                            int chkS = ROW_H - 4;
                            if (mx >= chkX && mx <= chkX + chkS) {
                                if (checkedForDelete.contains(i)) checkedForDelete.remove(i);
                                else checkedForDelete.add(i);
                            }
                        } else {
                            String[] parts = themes.get(i).split("\\|", 2);
                            int copyX = panelX + W - PAD - 34;
                            int editX = copyX - 30 - 3;
                            if (mx >= copyX && mx <= copyX + 32 && parts.length > 1) {
                                Minecraft.getInstance().keyboardHandler.setClipboard(parts[1]);
                            } else if (mx >= editX && mx <= editX + 28) {
                                if (editingMode && editingIndex == i) {
                                    editingMode = false; editingIndex = -1;
                                } else {
                                    editingMode = true; editingIndex = i; addingNew = false;
                                    tfName.value = parts.length > 0 ? parts[0] : ""; tfName.cursor = tfName.value.length(); tfName.clearSelection();
                                    tfCode.value = parts.length > 1 ? parts[1] : ""; tfCode.cursor = tfCode.value.length(); tfCode.clearSelection();
                                    focusedField = 0;
                                }
                            } else if (parts.length > 1) {
                                if (activeLibraryIndex == i) {
                                    revertLibraryPreview();
                                } else {
                                    if (previewOriginalCode == null) {
                                        previewOriginalCode = encodeTheme();
                                    }
                                    activeLibraryIndex = i;
                                    applyThemeCode(parts[1]);
                                    if (editingMode && editingIndex == i) { tfCode.value = parts[1]; tfCode.cursor = tfCode.value.length(); tfCode.clearSelection(); }
                                }
                            }
                        }
                        return true;
                    }
                    ey += ROW_H + PAD;
                }
                return true;
            }

            if (isFormOpen()) {
                net.minecraft.client.gui.Font f = Minecraft.getInstance().font;
                int fy  = formY();
                int fx  = panelX + PAD + 4;
                int fw  = W - PAD * 2 - 8;
                int lw  = f.width("Name:");
                int tfx = fx + lw + 4;
                int tfw = fw - lw - 4;
                int cy  = fy + PAD + ROW_H + PAD;
                if (mx >= tfx && mx <= tfx + tfw && my >= cy && my < cy + ROW_H) { tfName.clearSelection(); tfCode.clearSelection(); focusedField = 0; return true; }
                cy += ROW_H + PAD;
                if (mx >= tfx && mx <= tfx + tfw && my >= cy && my < cy + ROW_H) { tfName.clearSelection(); tfCode.clearSelection(); focusedField = 1; return true; }
                cy += ROW_H + PAD;
                int saveLbl = f.width("Save");
                int saveW   = saveLbl + 16;
                int saveX   = fx + (fw - saveW) / 2;
                if (mx >= saveX && mx <= saveX + saveW && my >= cy && my < cy + ROW_H) {
                    saveTheme(); return true;
                }
            }
            return true;
        }

        private void saveTheme() {
            String name = tfName.value.trim();
            if (name.isEmpty()) return;
            String code = tfCode.value.isBlank() ? encodeDefaultTheme() : tfCode.value.trim();
            String entry = name + "|" + code;
            if (editingMode && editingIndex >= 0 && editingIndex < MacroConfig.savedThemes.size()) {
                MacroConfig.savedThemes.set(editingIndex, entry);
                if (editingIndex == activeLibraryIndex) previewOriginalCode = null;
                tfCode.value = code; tfCode.cursor = code.length(); tfCode.clearSelection();
                editingMode = false; editingIndex = -1;
            } else {
                MacroConfig.savedThemes.add(0, entry);
                addingNew = false;
                tfCode.value = code; tfCode.cursor = code.length(); tfCode.clearSelection();
                if (activeLibraryIndex >= 0) activeLibraryIndex++;
            }
            MacroConfig.save();
        }

        private boolean hasPendingThemeChanges() {
            if (activeLibraryIndex < 0 || activeLibraryIndex >= MacroConfig.savedThemes.size()) return false;
            String[] sp = MacroConfig.savedThemes.get(activeLibraryIndex).split("\\|", 2);
            return sp.length < 2 || !encodeTheme().equals(sp[1]);
        }

        public void drag(int mx, int my) {
            if (!dragging) return;
            panelX = Math.max(0, Math.min(screenW - W, mx - dragOffX));
            panelY = Math.max(0, Math.min(screenH - calcTotalHeight(), my - dragOffY));
        }

        public void stopDrag() { dragging = false; }

        @Override
        public void scroll(int dir) {
            scrollOffset = Math.max(0, Math.min(maxScroll(), scrollOffset + dir * 10));
        }

        @Override
        public boolean keyPressed(int key, int scan, int mods) {
            if (!isFormOpen()) return false;
            if (key == 256) { addingNew = false; editingMode = false; editingIndex = -1; return true; }
            if ((key == 257 || key == 335) && (mods & GLFW.GLFW_MOD_SHIFT) == 0) {
                if (focusedField == 1 && !tfCode.value.isBlank() && !editingMode) { applyThemeCode(tfCode.value.trim()); }
                else { saveTheme(); }
                return true;
            }
            if (key == 258) { focusedField = (focusedField + 1) % 2; return true; }
            if (key == 259) {
                TextField _tf = focusedField == 0 ? tfName : tfCode;
                _tf.keyPressed(259, 0, 0, null);
                return true;
            }
            if (key == 86 && (mods & GLFW.GLFW_MOD_CONTROL) != 0) {
                String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
                if (clip != null) { TextField _tf = focusedField == 0 ? tfName : tfCode; _tf.insert(clip.replace("\r", "").replace("\n", "")); if (focusedField == 1 && !tfCode.value.isBlank()) applyThemeCode(tfCode.value.trim()); }
                return true;
            }
            { TextField _tf = focusedField == 0 ? tfName : tfCode; if (_tf.keyPressed(key, scan, mods, null)) { if (focusedField == 1 && !tfCode.value.isBlank()) applyThemeCode(tfCode.value.trim()); return true; } }
            if (key == GLFW.GLFW_KEY_SPACE) { (focusedField == 0 ? tfName : tfCode).insert(" "); return true; }
            Character c = fallbackCharFromKey(key, scan, mods);
            if (c != null) { (focusedField == 0 ? tfName : tfCode).insert(String.valueOf(c)); return true; }
            return false;
        }

        @Override
        public boolean charTyped(char c, int mods) {
            if (!isFormOpen() || c == '\r' || c == '\n') return false;
            if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) return true;
            if ((mods & GLFW.GLFW_MOD_SHIFT) != 0 && c >= 128) return true;
            (focusedField == 0 ? tfName : tfCode).insert(String.valueOf(c));
            if (focusedField == 1 && !tfCode.value.isBlank()) applyThemeCode(tfCode.value.trim());
            return true;
        }

        @Override public void commit() { tfName.clearSelection(); tfCode.clearSelection(); }
    }


    /**
     * Chat Rules entry — opens a full sub-panel for managing chat alert rules.
     * Each rule is stored as: "name|matchType|caseSensitive|pingWebhook|enabled|matchText"
     */
    static class ChatRulesEntry implements Entry {
        @Override
        public void render(GuiGraphics g, int x, int y, int w, int h, boolean hov, net.minecraft.client.gui.Font font) {
            if (hov) fillRoundRect(g, x + 2, y + 2, w - 4, h - 4, 3, C_HOVER());
            int count = MacroConfig.chatRules.size();
            String lbl = "Manage Rules (" + count + ")";
            int tw = font.width(lbl);
            MacroConfig.drawStyledText(g, font, lbl, x + (w - tw) / 2, y + h / 2 - 4, hov ? C_TXT() : C_DIM());
        }

        @Override
        public void onClick(int mx, int my) {}

        @Override
        public SubPanel openSubPanel(int mx, int my, int sw, int sh) {
            return new ChatRulesSubPanel(mx, my, sw, sh);
        }
    }

    /**
     * Encoding: "name|matchType|caseSensitive|pingWebhook|enabled|matchText"
     * MatchType limited to: Contains, Equals, StartsWith, EndsWith  (Regex removed from UI)
     * editingIndex: -1 = none, -2 = new rule, >=0 = editing existing rule
     */
    static class ChatRulesSubPanel implements SubPanel {
        private static final int W        = 340;
        private static final int ROW_H    = 18;
        private static final int PAD      = 4;
        private static final int HDR_H    = 16;
        private static final int LIST_MAX = 120;

        private final int screenW, screenH;
        private int panelX, panelY;
        private int scrollOffset = 0;

        private boolean dragging = false;
        private int dragOffX, dragOffY;

        private int editingIndex = -1;
        final TextField tfName = new TextField("");
        final TextField tfMatch = new TextField("");
        private static final com.ihanuat.mod.modules.ChatRuleManager.MatchType[] MATCH_TYPES = {
                com.ihanuat.mod.modules.ChatRuleManager.MatchType.Contains,
                com.ihanuat.mod.modules.ChatRuleManager.MatchType.Equals,
                com.ihanuat.mod.modules.ChatRuleManager.MatchType.StartsWith,
                com.ihanuat.mod.modules.ChatRuleManager.MatchType.EndsWith,
        };
        private com.ihanuat.mod.modules.ChatRuleManager.MatchType editMatchType =
                com.ihanuat.mod.modules.ChatRuleManager.MatchType.Contains;
        private boolean editCaseSensitive = false;
        private boolean editEnabled       = true;
        int focusedField = 0;
        private boolean cursorVisible = true;
        private long lastBlink = System.currentTimeMillis();

        ChatRulesSubPanel(int mx, int my, int sw, int sh) {
            this.screenW = sw; this.screenH = sh;
            int h = calcTotalHeight();
            this.panelX = Math.max(4, Math.min(mx, sw - W - 4));
            this.panelY = Math.max(4, Math.min(my, sh - h - 4));
        }

        private int listContentH() {
            return Math.max(PAD, MacroConfig.chatRules.size() * (ROW_H + PAD) + PAD);
        }
        private int listVisibleH() { return Math.min(listContentH(), LIST_MAX); }
        private int maxScroll()    { return Math.max(0, listContentH() - listVisibleH()); }
        private int editPanelH()   { return PAD + 4 * (ROW_H + PAD) + PAD; }
        private int calcTotalHeight() {
            int h = HDR_H + PAD
                    + listVisibleH() + PAD
                    + ROW_H + PAD;
            if (isEditing()) h += editPanelH() + PAD;
            return Math.min(h, screenH - 8);
        }
        private boolean isEditing() { return editingIndex == -2 || editingIndex >= 0; }

        private int listY()  { return panelY + HDR_H + PAD; }
        private int addBtnY(){ return listY() + listVisibleH() + PAD; }
        private int editY()  { return addBtnY() + ROW_H + PAD; }

        @Override
        public void render(GuiGraphics g, int mx, int my, net.minecraft.client.gui.Font font) {
            int totalH = calcTotalHeight();
            panelY = Math.max(4, Math.min(panelY, screenH - totalH - 4));

            fillRoundRect(g, panelX - 2, panelY - 2, W + 4, totalH + 4, 4, C_SPBD());
            fillRoundRect(g, panelX, panelY, W, totalH, 3, C_SPBG());

            fillRoundRect(g, panelX, panelY, W, HDR_H, PANEL_RADIUS, C_HDR());
            g.fill(panelX, panelY + HDR_H - PANEL_RADIUS, panelX + W, panelY + HDR_H, C_HDR());
            g.fill(panelX + 2, panelY + HDR_H - 1, panelX + W - 2, panelY + HDR_H, C_LINE());
            MacroConfig.drawStyledText(g, font, tr("Chat Rules", "聊天规则"), panelX + 5, panelY + HDR_H / 2 - 4, C_TXT());
            int crQx = panelX + W - 10 - font.width("?") - 4;
            boolean crQhov = mx >= crQx - 1 && mx <= crQx + font.width("?") + 3 && my >= panelY && my <= panelY + HDR_H;
            boolean crQactive = "chatrules".equals(helperTopic);
            if (crQhov || crQactive) fillRoundRect(g, crQx - 1, panelY + 2, font.width("?") + 4, HDR_H - 4, 2, crQactive ? C_ON() : C_HOVER());
            MacroConfig.drawStyledText(g, font, "?", crQx + 1, panelY + HDR_H / 2 - 4, crQactive || crQhov ? C_TXT() : C_DIM());
            MacroConfig.drawStyledText(g, font, "v", panelX + W - 10, panelY + HDR_H / 2 - 4, C_DIM());

            int lY = listY(), lH = listVisibleH();
            g.enableScissor(panelX, lY, panelX + W, lY + lH);
            int ey = lY - scrollOffset;
            List<String> rules = MacroConfig.chatRules;
            for (int i = 0; i < rules.size(); i++) {
                com.ihanuat.mod.modules.ChatRuleManager.ChatRule rule =
                        new com.ihanuat.mod.modules.ChatRuleManager.ChatRule(rules.get(i));
                boolean hov = mx >= panelX + PAD && mx <= panelX + W - PAD - 20
                        && my >= ey && my < ey + ROW_H;
                boolean sel = editingIndex == i;
                if (sel)      fillRoundRect(g, panelX + PAD, ey, W - PAD * 2, ROW_H, 2, C_ON());
                else if (hov) fillRoundRect(g, panelX + PAD, ey, W - PAD * 2, ROW_H, 2, C_HOVER());

                g.fill(panelX + PAD + 2, ey + 5, panelX + PAD + 7, ey + ROW_H - 5,
                        rule.enabled ? C_ON() : C_OFF());

                String name = truncate(font, rule.name, 110);
                MacroConfig.drawStyledText(g, font, name, panelX + PAD + 10, ey + (ROW_H - 8) / 2,
                        rule.enabled ? C_TXT() : C_DIM());

                String badge = rule.matchType.name().substring(0, Math.min(4, rule.matchType.name().length()));
                MacroConfig.drawStyledText(g, font, badge, panelX + PAD + 118, ey + (ROW_H - 8) / 2, C_DIM());

                String prev = truncate(font, rule.matchText, W - PAD * 2 - 165);
                MacroConfig.drawStyledText(g, font, prev, panelX + PAD + 158, ey + (ROW_H - 8) / 2, C_DIM());

                int delX = panelX + W - PAD - 18;
                boolean delHov = mx >= delX && mx <= delX + 16 && my >= ey + 1 && my < ey + ROW_H - 1;
                fillRoundRect(g, delX, ey + 2, 16, ROW_H - 4, 2, delHov ? 0xFFCC2222 : 0xFF441111);
                MacroConfig.drawStyledText(g, font, "X", delX + 4, ey + (ROW_H - 8) / 2, C_TXT());

                ey += ROW_H + PAD;
            }
            g.disableScissor();

            if (maxScroll() > 0) {
                int bh = Math.max(10, lH * lH / listContentH());
                int by = lY + (int)((lH - bh) * ((float) scrollOffset / maxScroll()));
                g.fill(panelX + W - 4, lY, panelX + W - 2, lY + lH, C_OFF());
                g.fill(panelX + W - 4, by, panelX + W - 2, by + bh, C_ACC());
            }

            int aY = addBtnY();
            boolean addHov = mx >= panelX + PAD && mx <= panelX + W - PAD && my >= aY && my < aY + ROW_H;
            fillRoundRect(g, panelX + PAD, aY, W - PAD * 2, ROW_H, 3, addHov ? C_BTN() : C_OFF());
            String addLbl = (editingIndex == -2) ? "Cancel" : "+ Add Rule";
            MacroConfig.drawStyledText(g, font, addLbl,
                    panelX + (W - font.width(addLbl)) / 2, aY + (ROW_H - 8) / 2, C_TXT());

            if (isEditing()) renderEditPanel(g, mx, my, font, editY());
        }

        private void renderEditPanel(GuiGraphics g, int mx, int my,
                                     net.minecraft.client.gui.Font font, int ry) {
            int ep = editPanelH();
            fillRoundRect(g, panelX + PAD - 2, ry, W - PAD * 2 + 4, ep, 3, C_SPBD());
            fillRoundRect(g, panelX + PAD, ry + 2, W - PAD * 2, ep - 2, 2, C_SBGR());

            int fx = panelX + PAD + 4;
            int fw = W - PAD * 2 - 8;
            int cy = ry + PAD;

            MacroConfig.drawStyledText(g, font, "Name:", fx, cy + (ROW_H - 8) / 2, C_DIM());
            int nfx = fx + 40;
            tfName.render(g, font, nfx, cy, fw - 40, ROW_H, focusedField == 0);
            cy += ROW_H + PAD;

            MacroConfig.drawStyledText(g, font, "Pattern:", fx, cy + (ROW_H - 8) / 2, C_DIM());
            int mfx = fx + 52;
            tfMatch.render(g, font, mfx, cy, fw - 52, ROW_H, focusedField == 1);
            cy += ROW_H + PAD;

            int ox = fx;
            ox = drawOptionBtn(g, font, mx, my, ox, cy, editMatchType.name(), true) + 3;
            ox = drawOptionBtn(g, font, mx, my, ox, cy, "Case", editCaseSensitive) + 3;
            drawOptionBtn(g, font, mx, my, ox, cy, "Active", editEnabled);
            cy += ROW_H + PAD;

            int hw = (fw - 4) / 2;
            boolean saveHov = mx >= fx && mx <= fx + hw && my >= cy && my < cy + ROW_H;
            fillRoundRect(g, fx, cy, hw, ROW_H, 2, saveHov ? C_BTN() : brighten(C_ON(), 0x111111));
            MacroConfig.drawStyledText(g, font, "Save",
                    fx + (hw - font.width("Save")) / 2, cy + (ROW_H - 8) / 2, C_TXT());
            int cx2 = fx + hw + 4;
            boolean cancelHov = mx >= cx2 && mx <= cx2 + hw && my >= cy && my < cy + ROW_H;
            fillRoundRect(g, cx2, cy, hw, ROW_H, 2, cancelHov ? C_BTN() : C_OFF());
            MacroConfig.drawStyledText(g, font, "Cancel",
                    cx2 + (hw - font.width("Cancel")) / 2, cy + (ROW_H - 8) / 2, C_TXT());
        }

        private void drawTextField(GuiGraphics g, net.minecraft.client.gui.Font font,
                                   int fx, int fy, int fw, String value, boolean focused) {
            g.fill(fx, fy, fx + fw, fy + ROW_H, C_SBGR());
            g.fill(fx, fy + ROW_H - 1, fx + fw, fy + ROW_H, focused ? C_ACC() : C_DIM());
            g.enableScissor(fx + 2, fy, fx + fw - 2, fy + ROW_H);
            String disp = value + (focused && cursorVisible ? "|" : "");
            while (font.width(disp) > fw - 6 && disp.length() > 1)
                disp = disp.substring(1);
            MacroConfig.drawStyledText(g, font, disp, fx + 3, fy + (ROW_H - 8) / 2, C_TXT());
            g.disableScissor();
        }

        private int drawOptionBtn(GuiGraphics g, net.minecraft.client.gui.Font font,
                                  int mx, int my, int bx, int by, String label, boolean active) {
            int bw = font.width(label) + 8;
            boolean hov = mx >= bx && mx <= bx + bw && my >= by && my < by + ROW_H;
            fillRoundRect(g, bx, by, bw, ROW_H, 2, active ? C_ON() : (hov ? C_HOVER() : C_OFF()));
            MacroConfig.drawStyledText(g, font, label, bx + 4, by + (ROW_H - 8) / 2,
                    active ? C_TXT() : C_DIM());
            return bx + bw;
        }

        /** Truncates s so that its rendered pixel width fits within maxPx. */
        private static String truncate(net.minecraft.client.gui.Font font, String s, int maxPx) {
            if (s == null) return "";
            if (font.width(s) <= maxPx) return s;
            while (s.length() > 0 && font.width(s + "..") > maxPx)
                s = s.substring(0, s.length() - 1);
            return s + "..";
        }

        @Override
        public boolean contains(int mx, int my) {
            int h = calcTotalHeight();
            return mx >= panelX - 2 && mx <= panelX + W + 2
                    && my >= panelY - 2 && my <= panelY + h + 2;
        }

        @Override
        public boolean mouseClicked(int mx, int my, int btn, net.minecraft.client.gui.Font font) {
            if (mx >= panelX && mx <= panelX + W && my >= panelY && my <= panelY + HDR_H) {
                net.minecraft.client.gui.Font f = Minecraft.getInstance().font;
                int crQx = panelX + W - 10 - f.width("?") - 4;
                if (mx >= crQx - 1 && mx <= crQx + f.width("?") + 3) {
                    if ("chatrules".equals(helperTopic)) {
                        helperTopic = null; helperTitle = null; helperScrollOffset = 0;
                    } else {
                        helperTopic = "chatrules"; helperTitle = tr("Chat Rules", "聊天规则"); helperScrollOffset = 0;
                    }
                    return true;
                }
                dragging = true;
                dragOffX = mx - panelX;
                dragOffY = my - panelY;
                return true;
            }

            int lY  = listY();
            int lH  = listVisibleH();
            int aY  = addBtnY();
            int eY  = editY();

            if (mx >= panelX + PAD && mx <= panelX + W - PAD
                    && my >= lY && my < lY + lH) {
                int ey = lY - scrollOffset;
                List<String> rules = MacroConfig.chatRules;
                for (int i = 0; i < rules.size(); i++) {
                    if (my >= ey && my < ey + ROW_H) {
                        int delX = panelX + W - PAD - 18;
                        if (mx >= delX && mx <= delX + 16) {
                            rules.remove(i);
                            MacroConfig.save();
                            if (editingIndex == i)     editingIndex = -1;
                            else if (editingIndex > i) editingIndex--;
                        } else {
                            if (editingIndex == i) editingIndex = -1;
                            else loadRule(i);
                        }
                        return true;
                    }
                    ey += ROW_H + PAD;
                }
                return true;
            }

            if (mx >= panelX + PAD && mx <= panelX + W - PAD
                    && my >= aY && my < aY + ROW_H) {
                if (editingIndex == -2) {
                    editingIndex = -1;
                } else {
                    editingIndex      = -2;
                    tfName.value = ""; tfName.cursor = 0; tfName.clearSelection();
                    tfMatch.value = ""; tfMatch.cursor = 0; tfMatch.clearSelection();
                    editMatchType     = com.ihanuat.mod.modules.ChatRuleManager.MatchType.Contains;
                    editCaseSensitive = false;
                    editEnabled       = true;
                    focusedField      = 0;
                }
                return true;
            }

            if (!isEditing()) return true;
            int fx = panelX + PAD + 4;
            int fw = W - PAD * 2 - 8;
            int cy = eY + PAD;

            int nfx = fx + 40;
            if (mx >= nfx && mx <= nfx + fw - 40 && my >= cy && my < cy + ROW_H) {
                tfName.clearSelection(); tfMatch.clearSelection(); focusedField = 0; return true;
            }
            cy += ROW_H + PAD;

            int mfx = fx + 52;
            if (mx >= mfx && mx <= mfx + fw - 52 && my >= cy && my < cy + ROW_H) {
                tfName.clearSelection(); tfMatch.clearSelection(); focusedField = 1; return true;
            }
            cy += ROW_H + PAD;

            {
                net.minecraft.client.gui.Font f = net.minecraft.client.Minecraft.getInstance().font;
                int ox = fx; int bw;

                bw = f.width(editMatchType.name()) + 8;
                if (mx >= ox && mx <= ox + bw && my >= cy && my < cy + ROW_H) {
                    int cur = 0;
                    for (int i = 0; i < MATCH_TYPES.length; i++)
                        if (MATCH_TYPES[i] == editMatchType) { cur = i; break; }
                    editMatchType = MATCH_TYPES[(cur + 1) % MATCH_TYPES.length];
                    return true;
                }
                ox += bw + 3;

                bw = f.width("Case") + 8;
                if (mx >= ox && mx <= ox + bw && my >= cy && my < cy + ROW_H) {
                    editCaseSensitive = !editCaseSensitive; return true;
                }
                ox += bw + 3;

                bw = f.width("Active") + 8;
                if (mx >= ox && mx <= ox + bw && my >= cy && my < cy + ROW_H) {
                    editEnabled = !editEnabled; return true;
                }
            }
            cy += ROW_H + PAD;

            int hw = (fw - 4) / 2;
            if (mx >= fx && mx <= fx + hw && my >= cy && my < cy + ROW_H) {
                saveRule(); return true;
            }
            int cx2 = fx + hw + 4;
            if (mx >= cx2 && mx <= cx2 + hw && my >= cy && my < cy + ROW_H) {
                editingIndex = -1; return true;
            }
            return true;
        }

        private boolean hasPendingThemeChanges() {
            if (activeLibraryIndex < 0 || activeLibraryIndex >= MacroConfig.savedThemes.size()) return false;
            String[] sp = MacroConfig.savedThemes.get(activeLibraryIndex).split("\\|", 2);
            return sp.length < 2 || !encodeTheme().equals(sp[1]);
        }

        public void drag(int mx, int my) {
            if (!dragging) return;
            panelX = Math.max(0, Math.min(screenW - W, mx - dragOffX));
            panelY = Math.max(0, Math.min(screenH - calcTotalHeight(), my - dragOffY));
        }

        public void stopDrag() { dragging = false; }

        private void loadRule(int idx) {
            com.ihanuat.mod.modules.ChatRuleManager.ChatRule rule =
                    new com.ihanuat.mod.modules.ChatRuleManager.ChatRule(
                            MacroConfig.chatRules.get(idx));
            tfName.value = rule.name; tfName.cursor = tfName.value.length(); tfName.clearSelection();
            tfMatch.value = rule.matchText; tfMatch.cursor = tfMatch.value.length(); tfMatch.clearSelection();
            editMatchType = rule.matchType == com.ihanuat.mod.modules.ChatRuleManager.MatchType.Regex
                    ? com.ihanuat.mod.modules.ChatRuleManager.MatchType.Contains : rule.matchType;
            editCaseSensitive = rule.caseSensitive;
            editEnabled       = rule.enabled;
            editingIndex      = idx;
            focusedField      = 0;
        }

        private void saveRule() {
            if (tfName.value.isBlank()) return;
            String encoded = tfName.value + "|" + editMatchType.name() + "|"
                    + editCaseSensitive + "|true|"
                    + editEnabled + "|" + tfMatch.value;
            List<String> rules = MacroConfig.chatRules;
            if (editingIndex == -2)                                    rules.add(encoded);
            else if (editingIndex >= 0 && editingIndex < rules.size()) rules.set(editingIndex, encoded);
            MacroConfig.save();
            editingIndex = -1;
        }

        @Override
        public void scroll(int dir) {
            scrollOffset = Math.max(0, Math.min(maxScroll(), scrollOffset + dir * 10));
        }

        @Override
        public boolean keyPressed(int key, int scan, int mods) {
            if (!isEditing()) return false;
            if (key == 256) { editingIndex = -1; return true; }
            if (key == 257 || key == 335) { saveRule(); return true; }
            if (key == 258) {
                tfName.clearSelection(); tfMatch.clearSelection();
                focusedField = (focusedField + 1) % 2;
                return true;
            }
            TextField _tf = focusedField == 0 ? tfName : tfMatch;
            if (_tf.keyPressed(key, scan, mods, null)) return true;
            Character c = fallbackCharFromKey(key, scan, mods);
            if (c != null) { _tf.insert(String.valueOf(c)); return true; }
            return false;
        }

        @Override
        public boolean charTyped(char c, int mods) {
            if (!isEditing()) return false;
            if (c == '\r' || c == '\n') return true;
            if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) return true;
            if ((mods & GLFW.GLFW_MOD_SHIFT) != 0 && c >= 128) return true;
            (focusedField == 0 ? tfName : tfMatch).insert(String.valueOf(c));
            return true;
        }

        @Override public void commit() { tfName.clearSelection(); tfMatch.clearSelection(); }
    }

    static class ScriptSelectorEntry implements Entry {
        static final String[][] SCRIPTS = {
                {"netherwart:1", "Wart/Crops - S-Shape"},
                {"netherwart:0", "Wart/Crops - Vertical"},
                {"sugarcane:classical", "Sugarcane/Flowers - Classical"},
                {"sugarcane:sshape", "Sugarcane/Flowers - S-Shape"},
                {"cocoa", "Cocoa"},
                {"cactus", "Cactus"},
                {"mushroom:0", "Mushroom - Classical"},
                {"mushroom:1", "Mushroom - Staircase"},
                {"pumpkin:1", "Pumpkin/Melon"},
                {"echo", "Echo"}
        };

        static String displayName() {
            for (String[] s : SCRIPTS)
                if (s[0].equals(MacroConfig.restartScript)) return s[1];
            return MacroConfig.restartScript;
        }

        static void cycle(int dir) {
            int cur = 0;
            for (int i = 0; i < SCRIPTS.length; i++)
                if (SCRIPTS[i][0].equals(MacroConfig.restartScript)) {
                    cur = i;
                    break;
                }
            int next = (cur + dir + SCRIPTS.length) % SCRIPTS.length;
            MacroConfig.restartScript = SCRIPTS[next][0];
            save();
        }

        @Override
        public void render(GuiGraphics g, int x, int y, int w, int h, boolean hov, net.minecraft.client.gui.Font font) {
            MacroConfig.drawStyledText(g, font, tr("Farm Script", "农场脚本"), x + 2, y + h / 2 - 4, hov ? C_TXT() : C_DIM());
            String disp = displayName();
            int dw = font.width(disp);
            while (dw > w - font.width(tr("Farm Script", "农场脚本")) - 20 && disp.length() > 4) {
                disp = disp.substring(0, disp.length() - 1);
                dw = font.width(disp + "..");
            }
            if (!disp.equals(displayName())) disp += "..";
            MacroConfig.drawStyledText(g, font, disp, x + w - font.width(disp) - 2, y + h / 2 - 4, C_ON2());
        }

        @Override
        public void onClick(int mx, int my) {
            cycle(1);
        }

        @Override
        public SubPanel openSubPanel(int mx, int my, int sw, int sh) {
            return new ScriptPickerSubPanel(mx, my, sw, sh);
        }
    }

    static class ScriptPickerSubPanel implements SubPanel {
        final int x, y, w = 240;
        static final int ROW_H = 16, PAD = 3;

        int h() {
            return PAD + ScriptSelectorEntry.SCRIPTS.length * (ROW_H + PAD) + PAD + 14;
        }

        ScriptPickerSubPanel(int mx, int my, int sw, int sh) {
            this.x = Math.min(mx, sw - w - 4);
            this.y = Math.min(my, sh - h() - 4);
        }

        @Override
        public void render(GuiGraphics g, int mx, int my, net.minecraft.client.gui.Font font) {
            int h = h();
            fillRoundRect(g, x - 2, y - 2, w + 4, h + 4, 4, C_SPBD());
            fillRoundRect(g, x, y, w, h, 3, C_SPBG());
            MacroConfig.drawStyledText(g, font, tr("Farm Script", "农场脚本"), x + 4, y + 4, C_TXT());
            g.fill(x + 4, y + 14, x + w - 4, y + 15, C_ACC());
            int ey = y + 18;
            for (String[] s : ScriptSelectorEntry.SCRIPTS) {
                boolean selected = s[0].equals(MacroConfig.restartScript);
                boolean hov = mx >= x + 4 && mx <= x + w - 4 && my >= ey && my <= ey + ROW_H;
                if (hov || selected) fillRoundRect(g, x + 4, ey, w - 8, ROW_H, 2, selected ? C_ON() : C_HOVER());
                MacroConfig.drawStyledText(g, font, s[1], x + 8, ey + ROW_H / 2 - 4, selected ? C_TXT() : C_DIM());
                ey += ROW_H + PAD;
            }
        }

        @Override
        public boolean contains(int mx, int my) {
            return mx >= x - 2 && mx <= x + w + 2 && my >= y - 2 && my <= y + h() + 2;
        }

        @Override
        public boolean mouseClicked(int mx, int my, int btn, net.minecraft.client.gui.Font font) {
            int ey = y + 18;
            for (String[] s : ScriptSelectorEntry.SCRIPTS) {
                if (my >= ey && my <= ey + ROW_H && mx >= x + 4 && mx <= x + w - 4) {
                    MacroConfig.restartScript = s[0];
                    save();
                    return true;
                }
                ey += ROW_H + ScriptPickerSubPanel.PAD;
            }
            return true;
        }

        @Override
        public void scroll(int dir) {
        }

        @Override
        public boolean keyPressed(int key, int scan, int mods) {
            return false;
        }

        @Override
        public boolean charTyped(char c, int mods) {
            return false;
        }

        @Override
        public void commit() {
        }
    }

    interface SubPanel {
        void render(GuiGraphics g, int mx, int my, net.minecraft.client.gui.Font font);

        boolean contains(int mx, int my);

        boolean mouseClicked(int mx, int my, int btn, net.minecraft.client.gui.Font font);

        boolean keyPressed(int key, int scan, int mods);

        boolean charTyped(char c, int mods);

        void scroll(int dir);

        void commit();
    }

    static class ColorSubPanel implements SubPanel {
        final String label;
        int r, g, b, a;
        final Consumer<Integer> setter;
        final boolean supportsAlpha;
        final int x, y, w = 260, h = 110;
        int draggingSlider = -1;
        boolean editingHex = false;
        String hexBuffer = "";
        boolean cursorVisible = true;
        long lastBlink = System.currentTimeMillis();

        ColorSubPanel(int mx, int my, int sw, int sh, String label, int initial, Consumer<Integer> setter) {
            this(mx, my, sw, sh, label, initial, setter, true);
        }
        ColorSubPanel(int mx, int my, int sw, int sh, String label, int initial, Consumer<Integer> setter, boolean supportsAlpha) {
            this.supportsAlpha = supportsAlpha;
            this.label = label;
            this.setter = setter;
            a = (initial >> 24) & 0xFF;
            r = (initial >> 16) & 0xFF;
            g = (initial >> 8) & 0xFF;
            b = initial & 0xFF;
            if (a == 0 && r == 0 && g == 0 && b == 0) a = 255;
            this.x = Math.min(mx, sw - w - 4);
            this.y = Math.min(my, sh - h - 4);
        }

        int packed() {
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        int getChannel(int i) {
            return i == 0 ? r : i == 1 ? g : i == 2 ? b : a;
        }

        void setChannel(int i, int v) {
            if (i == 0) r = v;
            else if (i == 1) g = v;
            else if (i == 2) b = v;
            else a = v;
        }

        int sliderBx() {
            return x + 16;
        }

        int sliderBw() {
            return w - 50;
        }

        int sliderSy(int i) {
            return y + 32 + i * 18;
        }

        void applyDrag(int mx) {
            if (draggingSlider < 0) return;
            int bx = sliderBx(), bw2 = sliderBw();
            int val = Math.max(0, Math.min(255, Math.round((mx - bx) / (float) bw2 * 255)));
            setChannel(draggingSlider, val);
            setter.accept(packed());
            save();
        }

        @Override
        public void render(GuiGraphics g2, int mx, int my, net.minecraft.client.gui.Font font) {
            fillRoundRect(g2, x - 2, y - 2, w + 4, h + 4, 4, C_SPBD());
            fillRoundRect(g2, x, y, w, h, 3, C_SPBG());

            g2.drawString(font, label, x + 4, y + 4, C_TXT(), false);

            int sw2 = 20;
            int sx = x + w - sw2 - 4, sy0 = y + 4;
            g2.fill(sx - 1, sy0 - 1, sx + sw2 + 1, sy0 + sw2 + 1, 0xFF555555);
            g2.fill(sx, sy0, sx + sw2, sy0 + sw2, packed());

            if (System.currentTimeMillis() - lastBlink > 500) {
                cursorVisible = !cursorVisible;
                lastBlink = System.currentTimeMillis();
            }
            String hexDisplay = editingHex ? hexBuffer + (cursorVisible ? "|" : "") : String.format("#%08X", packed());
            int hexColor = editingHex ? C_TXT() : C_DIM();
            g2.fill(x + 4, y + 15, x + w - sw2 - 10, y + 27, C_SBGR());
            g2.fill(x + 4, y + 27, x + w - sw2 - 10, y + 28, editingHex ? C_ACC() : 0xFF333355);
            g2.drawString(font, hexDisplay, x + 6, y + 17, hexColor, false);
            if (!editingHex)
                g2.drawString(font, "click hex to edit", x + w - sw2 - 9 - font.width("click hex to edit"), y + 18, C_DIM(), false);

            int[] cols = {0xFFCC4444, 0xFF44CC44, 0xFF4444CC, 0xFFAAAAAA};
            String[] names = {"R", "G", "B", "A"};
            int bx = sliderBx(), bw2 = sliderBw();
            for (int i = 0; i < (supportsAlpha ? 4 : 3); i++) {
                int val = getChannel(i);
                int sy = sliderSy(i);
                g2.drawString(font, names[i], x + 4, sy + 1, cols[i], false);
                fillRoundRect(g2, bx, sy + 3, bw2, 6, 2, C_SBGR());
                int fw = (int) (val / 255f * bw2);
                if (fw > 0) fillRoundRect(g2, bx, sy + 3, fw, 6, 2, cols[i]);
                boolean hov = draggingSlider == i || (mx >= bx && mx <= bx + bw2 && my >= sy && my <= sy + 10);
                int kx = bx + fw - 3;
                g2.fill(Math.max(bx, kx), sy + 1, Math.max(bx, kx) + 6, sy + 9, hov ? 0xFFFFFFFF : 0xFFCCCCCC);
                g2.drawString(font, String.valueOf(val), bx + bw2 + 4, sy + 1, cols[i], false);
            }
        }

        @Override
        public boolean contains(int mx, int my) {
            return mx >= x - 2 && mx <= x + w + 2 && my >= y - 2 && my <= y + h + 2;
        }

        @Override
        public boolean mouseClicked(int mx, int my, int btn, net.minecraft.client.gui.Font font) {
            int sw2 = 20;
            if (mx >= x + 4 && mx <= x + w - sw2 - 10 && my >= y + 15 && my <= y + 28) {
                editingHex = !editingHex;
                if (editingHex) hexBuffer = String.format("%08X", packed());
                return true;
            }
            editingHex = false;
            int bx = sliderBx(), bw2 = sliderBw();
            for (int i = 0; i < (supportsAlpha ? 4 : 3); i++) {
                int sy = sliderSy(i);
                if (my >= sy && my <= sy + 10 && mx >= bx && mx <= bx + bw2) {
                    draggingSlider = i;
                    applyDrag(mx);
                    return true;
                }
            }
            return true;
        }

        public void drag(int mx) {
            applyDrag(mx);
        }

        @Override
        public void scroll(int dir) {
        }

        @Override
        public boolean keyPressed(int key, int scan, int mods) {
            if (editingHex) {
                if (key == 259 && !hexBuffer.isEmpty()) {
                    hexBuffer = hexBuffer.substring(0, hexBuffer.length() - 1);
                    return true;
                }
                if (key == 257 || key == 335) {
                    applyHex();
                    editingHex = false;
                    return true;
                }
                if (key == 256) {
                    editingHex = false;
                    return true;
                }
                if (key == 65 && (mods & org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL) != 0) { hexBuffer = ""; return true; }
                if (key == 86 && (mods & org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL) != 0) {
                    String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
                    if (clip != null) hexBuffer += clip.replaceAll("[^0-9a-fA-F#]", "");
                    return true;
                }
                Character c = fallbackCharFromKey(key, scan, mods);
                if (c != null && (Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
                    if (hexBuffer.length() < 8) hexBuffer += c;
                    return true;
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean charTyped(char c, int mods) {
            if (editingHex && (Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
                if (hexBuffer.length() < 8) hexBuffer += c;
                return true;
            }
            return false;
        }

        void applyHex() {
            try {
                String h = hexBuffer.replaceAll("#", "");
                long v = Long.parseLong(h, 16);
                if (h.length() <= 6) {
                    r = (int) ((v >> 16) & 0xFF);
                    g = (int) ((v >> 8) & 0xFF);
                    b = (int) (v & 0xFF);
                } else {
                    a = (int) ((v >> 24) & 0xFF);
                    r = (int) ((v >> 16) & 0xFF);
                    g = (int) ((v >> 8) & 0xFF);
                    b = (int) (v & 0xFF);
                }
                setter.accept(packed());
                save();
            } catch (Exception ignored) {
            }
        }

        @Override
        public void commit() {
            setter.accept(packed());
            save();
        }
    }

    static class TextField {
        String value;
        int cursor = 0;
        int selStart = -1, selEnd = -1;
        boolean cursorVisible = true;
        long lastBlink = System.currentTimeMillis();

        TextField(String initial) { this.value = initial; this.cursor = initial.length(); }

        boolean hasSelection() { return selStart >= 0 && selEnd >= 0 && selStart != selEnd; }
        int selMin() { return Math.min(selStart, selEnd); }
        int selMax() { return Math.max(selStart, selEnd); }
        String selected() { return hasSelection() ? value.substring(selMin(), selMax()) : ""; }

        void selectAll() { selStart = 0; selEnd = value.length(); cursor = value.length(); }
        void clearSelection() { selStart = -1; selEnd = -1; }

        void deleteSelection() {
            if (!hasSelection()) return;
            int lo = selMin(), hi = selMax();
            value = value.substring(0, lo) + value.substring(hi);
            cursor = lo;
            clearSelection();
        }

        void insert(String s) {
            if (hasSelection()) deleteSelection();
            value = value.substring(0, cursor) + s + value.substring(cursor);
            cursor += s.length();
        }

        void moveCursor(int pos) { cursor = Math.max(0, Math.min(value.length(), pos)); clearSelection(); }

        boolean keyPressed(int key, int scan, int mods, java.util.function.Predicate<Character> charFilter) {
            boolean ctrl = (mods & GLFW.GLFW_MOD_CONTROL) != 0;
            if (key == 65 && ctrl) { selectAll(); return true; }
            if (key == 67 && ctrl) {
                String sel = hasSelection() ? selected() : value;
                if (!sel.isEmpty()) Minecraft.getInstance().keyboardHandler.setClipboard(sel);
                return true;
            }
            if (key == 86 && ctrl) {
                String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
                if (clip != null) insert(clip.replaceAll("[\\r\\n]", ""));
                return true;
            }
            if (key == 259) {
                if (hasSelection()) deleteSelection();
                else if (cursor > 0) { value = value.substring(0, cursor - 1) + value.substring(cursor); cursor--; }
                return true;
            }
            if (key == GLFW.GLFW_KEY_LEFT)  { moveCursor(cursor - 1); return true; }
            if (key == GLFW.GLFW_KEY_RIGHT) { moveCursor(cursor + 1); return true; }
            if (key == GLFW.GLFW_KEY_HOME)  { moveCursor(0); return true; }
            if (key == GLFW.GLFW_KEY_END)   { moveCursor(value.length()); return true; }
            return false;
        }

        boolean charTyped(char ch, java.util.function.Predicate<Character> filter) {
            if (filter != null && !filter.test(ch)) return false;
            insert(String.valueOf(ch));
            return true;
        }

        void tickBlink() {
            long now = System.currentTimeMillis();
            if (now - lastBlink > 500) { cursorVisible = !cursorVisible; lastBlink = now; }
        }

        void render(GuiGraphics g, net.minecraft.client.gui.Font font, int fx, int fy, int fw, int fh) {
            render(g, font, fx, fy, fw, fh, true);
        }

        void render(GuiGraphics g, net.minecraft.client.gui.Font font, int fx, int fy, int fw, int fh, boolean focused) {
            if (focused) tickBlink();
            g.fill(fx, fy, fx + fw, fy + fh, C_SBGR());
            g.fill(fx, fy + fh - 1, fx + fw, fy + fh, focused ? C_ACC() : C_DIM());
            int textY = fy + (fh - 8) / 2;
            int textX = fx + 3;
            int maxW = fw - 6;

            String pre = value.substring(0, cursor);
            int fullPreW = font.width(pre);
            int scrollX = Math.max(0, fullPreW - maxW + 2);

            g.enableScissor(fx, fy, fx + fw, fy + fh);

            if (hasSelection()) {
                int lo = selMin(), hi = selMax();
                String sPre = value.substring(0, lo);
                String sSel = value.substring(lo, hi);
                int selX0 = textX + font.width(sPre) - scrollX;
                int selX1 = selX0 + font.width(sSel);
                g.fill(Math.max(fx, selX0), fy + 1, Math.min(fx + fw, selX1), fy + fh - 1, 0x664488FF);
            }

            MacroConfig.drawStyledText(g, font, value, textX - scrollX, textY, C_TXT());

            if (focused && cursorVisible && !hasSelection()) {
                int cx = textX + fullPreW - scrollX;
                g.fill(cx, textY - 1, cx + 1, textY + 9, C_TXT());
            }

            g.disableScissor();
        }
    }

    static class StringInputSubPanel implements SubPanel {
        final String label;
        final TextField tf;
        final String defaultValue;
        final Consumer<String> setter;
        final String placeholder;
        int w = -1;
        final int x, y, h = 50, screenW;

        StringInputSubPanel(int mx, int my, int sw, int sh, String label, String initial, Consumer<String> setter) {
            this(mx, my, sw, sh, label, initial, initial, setter, "");
        }
        StringInputSubPanel(int mx, int my, int sw, int sh, String label, String initial, String defaultVal, Consumer<String> setter) {
            this(mx, my, sw, sh, label, initial, defaultVal, setter, "");
        }
        StringInputSubPanel(int mx, int my, int sw, int sh, String label, String initial, String defaultVal, Consumer<String> setter, String placeholder) {
            this.label = label; this.tf = new TextField(initial); this.defaultValue = defaultVal;
            this.setter = setter; this.placeholder = placeholder == null ? "" : placeholder;
            this.screenW = sw; this.x = mx; this.y = Math.min(my, sh - h - 4);
        }

        private void ensureWidth(net.minecraft.client.gui.Font font) {
            if (w < 0) w = Math.max(220, font.width(label) + 80);
        }
        private int rx() { return Math.min(x, screenW - w - 4); }
        private int resetBtnX(net.minecraft.client.gui.Font font) { return rx() + w - font.width("reset") - 8; }
        private boolean overReset(int mx, int my, net.minecraft.client.gui.Font font) {
            int bx = resetBtnX(font);
            return mx >= bx - 2 && mx <= bx + font.width("reset") + 4 && my >= y + 3 && my <= y + 14;
        }

        @Override public void render(GuiGraphics g, int mx, int my, net.minecraft.client.gui.Font font) {
            ensureWidth(font);
            int rx = rx();
            fillRoundRect(g, rx - 2, y - 2, w + 4, h + 4, 4, C_SPBD());
            fillRoundRect(g, rx, y, w, h, 3, C_SPBG());
            MacroConfig.drawStyledText(g, font, label, rx + 5, y + 6, C_TXT());
            MacroConfig.drawStyledText(g, font, tr("reset", "重置"), resetBtnX(font), y + 4, overReset(mx, my, font) ? 0xFFFF5555 : C_DIM());
            if (tf.value.isEmpty() && !placeholder.isEmpty()) {
                g.fill(rx + 4, y + 20, rx + w - 4, y + 42, C_SBGR());
                g.fill(rx + 4, y + 42, rx + w - 4, y + 43, C_ACC());
                MacroConfig.drawStyledText(g, font, placeholder, rx + 6, y + 26, C_DIM());
            } else {
                tf.render(g, font, rx + 4, y + 20, w - 8, 22);
            }
        }
        @Override public boolean contains(int mx, int my) {
            if (w < 0) return false;
            int rx = rx(); return mx >= rx - 2 && mx <= rx + w + 2 && my >= y - 2 && my <= y + h + 2;
        }
        @Override public boolean mouseClicked(int mx, int my, int btn, net.minecraft.client.gui.Font font) {
            if (overReset(mx, my, font)) { tf.value = defaultValue; tf.cursor = tf.value.length(); tf.clearSelection(); commit(); }
            return true;
        }
        @Override public void scroll(int dir) {}
        @Override public boolean keyPressed(int key, int scan, int mods) {
            if (key == 257 || key == 335) { commit(); return true; }
            if (key == 65 && (mods & GLFW.GLFW_MOD_CONTROL) != 0) return true;
            if (tf.keyPressed(key, 0, mods, null)) return true;
            Character ch = fallbackCharFromKey(key, 0, mods);
            if (ch != null) { tf.charTyped(ch, null); return true; }
            return false;
        }
        @Override public boolean charTyped(char c, int mods) {
            if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) return true;
            if ((mods & GLFW.GLFW_MOD_SHIFT) != 0 && c >= 128) return true;
            tf.charTyped(c, null); return true;
        }
        @Override public void commit() { tf.clearSelection(); setter.accept(tf.value); }
    }

    static class IntInputSubPanel implements SubPanel {
        final String label;
        final TextField tf;
        final String defaultRaw;
        final int min, max;
        final Consumer<Integer> setter;
        int w = -1;
        final int x, y, h = 50, screenW;

        IntInputSubPanel(int mx, int my, int sw, int sh, String label, int initial, String defaultVal, int min, int max, Consumer<Integer> setter) {
            this.label = label; this.tf = new TextField(String.valueOf(initial)); this.defaultRaw = defaultVal;
            this.min = min; this.max = max; this.setter = setter;
            this.screenW = sw; this.x = mx; this.y = Math.min(my, sh - h - 4);
        }

        private java.util.function.Predicate<Character> intFilter() {
            return ch -> Character.isDigit(ch) || (ch == '-' && tf.value.isEmpty() && !tf.hasSelection());
        }
        private void ensureWidth(net.minecraft.client.gui.Font font) {
            if (w < 0) {
                String range = min == Integer.MIN_VALUE ? "" : "[" + min + "-" + max + "]";
                w = Math.max(200, font.width(label) + font.width(range) + 60);
            }
        }
        private int rx() { return Math.min(x, screenW - w - 4); }
        private int resetBtnX(net.minecraft.client.gui.Font font) { return rx() + w - font.width("reset") - 8; }
        private boolean overReset(int mx, int my, net.minecraft.client.gui.Font font) {
            int bx = resetBtnX(font);
            return mx >= bx - 2 && mx <= bx + font.width("reset") + 4 && my >= y + 3 && my <= y + 14;
        }

        @Override public void render(GuiGraphics g, int mx, int my, net.minecraft.client.gui.Font font) {
            ensureWidth(font);
            int rx = rx();
            fillRoundRect(g, rx - 2, y - 2, w + 4, h + 4, 4, C_SPBD());
            fillRoundRect(g, rx, y, w, h, 3, C_SPBG());
            MacroConfig.drawStyledText(g, font, label, rx + 5, y + 6, C_TXT());
            String range = min == Integer.MIN_VALUE ? "" : "[" + min + "-" + max + "]";
            MacroConfig.drawStyledText(g, font, range, rx + w - font.width(range) - font.width("reset") - 18, y + 6, C_DIM());
            MacroConfig.drawStyledText(g, font, tr("reset", "重置"), resetBtnX(font), y + 4, overReset(mx, my, font) ? 0xFFFF5555 : C_DIM());
            tf.render(g, font, rx + 4, y + 20, w - 8, 22);
        }
        @Override public boolean contains(int mx, int my) {
            if (w < 0) return false;
            int rx = rx(); return mx >= rx - 2 && mx <= rx + w + 2 && my >= y - 2 && my <= y + h + 2;
        }
        @Override public boolean mouseClicked(int mx, int my, int btn, net.minecraft.client.gui.Font font) {
            if (overReset(mx, my, font)) { tf.value = defaultRaw; tf.cursor = tf.value.length(); tf.clearSelection(); commit(); }
            return true;
        }
        @Override public void scroll(int dir) {}
        @Override public boolean keyPressed(int key, int scan, int mods) {
            if (key == 257 || key == 335) { commit(); return true; }
            if (tf.keyPressed(key, scan, mods, intFilter())) return true;
            if ((mods & GLFW.GLFW_MOD_CONTROL) == 0) {
                String name = GLFW.glfwGetKeyName(key, scan);
                if (name != null && name.length() == 1) { tf.charTyped(name.charAt(0), intFilter()); return true; }
                if ((mods & GLFW.GLFW_MOD_SHIFT) != 0 && key >= 48 && key <= 57) { tf.charTyped((char) key, intFilter()); return true; }
                if (key >= 320 && key <= 329) { tf.charTyped((char)('0' + key - 320), intFilter()); return true; }
            }
            return false;
        }
        @Override public boolean charTyped(char c, int mods) {
            if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) return true;
            tf.charTyped(c, intFilter()); return true;
        }
        @Override public void commit() {
            tf.clearSelection();
            try { int v = Integer.parseInt(tf.value); setter.accept(min == Integer.MIN_VALUE ? v : Math.max(min, Math.min(max, v))); }
            catch (NumberFormatException ignored) {}
        }
    }

    static class DoubleInputSubPanel implements SubPanel {
        final String label;
        final TextField tf;
        final String defaultRaw;
        final Consumer<Double> setter;
        int w = -1;
        final int x, y, h = 50, screenW;

        DoubleInputSubPanel(int mx, int my, int sw, int sh, String label, double initial, String defaultVal, Consumer<Double> setter) {
            this.label = label; this.tf = new TextField(trimDouble(initial)); this.defaultRaw = defaultVal;
            this.setter = setter; this.screenW = sw; this.x = mx; this.y = Math.min(my, sh - h - 4);
        }

        private java.util.function.Predicate<Character> dblFilter() {
            return ch -> Character.isDigit(ch)
                    || (ch == '-' && tf.value.isEmpty() && !tf.hasSelection())
                    || (ch == '.' && !tf.value.contains("."));
        }
        private void ensureWidth(net.minecraft.client.gui.Font font) {
            if (w < 0) w = Math.max(200, font.width(label) + 80);
        }
        private int rx() { return Math.min(x, screenW - w - 4); }
        private int resetBtnX(net.minecraft.client.gui.Font font) { return rx() + w - font.width("reset") - 8; }
        private boolean overReset(int mx, int my, net.minecraft.client.gui.Font font) {
            int bx = resetBtnX(font);
            return mx >= bx - 2 && mx <= bx + font.width("reset") + 4 && my >= y + 3 && my <= y + 14;
        }

        @Override public void render(GuiGraphics g, int mx, int my, net.minecraft.client.gui.Font font) {
            ensureWidth(font);
            int rx = rx();
            fillRoundRect(g, rx - 2, y - 2, w + 4, h + 4, 4, C_SPBD());
            fillRoundRect(g, rx, y, w, h, 3, C_SPBG());
            MacroConfig.drawStyledText(g, font, label, rx + 5, y + 6, C_TXT());
            MacroConfig.drawStyledText(g, font, tr("reset", "重置"), resetBtnX(font), y + 4, overReset(mx, my, font) ? 0xFFFF5555 : C_DIM());
            tf.render(g, font, rx + 4, y + 20, w - 8, 22);
        }
        @Override public boolean contains(int mx, int my) {
            if (w < 0) return false;
            int rx = rx(); return mx >= rx - 2 && mx <= rx + w + 2 && my >= y - 2 && my <= y + h + 2;
        }
        @Override public boolean mouseClicked(int mx, int my, int btn, net.minecraft.client.gui.Font font) {
            if (overReset(mx, my, font)) { tf.value = defaultRaw; tf.cursor = tf.value.length(); tf.clearSelection(); commit(); }
            return true;
        }
        @Override public void scroll(int dir) {}
        @Override public boolean keyPressed(int key, int scan, int mods) {
            if (key == 257 || key == 335) { commit(); return true; }
            if (tf.keyPressed(key, scan, mods, dblFilter())) return true;
            if ((mods & GLFW.GLFW_MOD_CONTROL) == 0) {
                String name = GLFW.glfwGetKeyName(key, scan);
                if (name != null && name.length() == 1) { tf.charTyped(name.charAt(0), dblFilter()); return true; }
                if ((mods & GLFW.GLFW_MOD_SHIFT) != 0 && key >= 48 && key <= 57) { tf.charTyped((char) key, dblFilter()); return true; }
                if (key >= 320 && key <= 329) { tf.charTyped((char)('0' + key - 320), dblFilter()); return true; }
                if ((key == 330 || key == 46) && !tf.value.contains(".")) { tf.charTyped('.', dblFilter()); return true; }
            }
            return false;
        }
        @Override public boolean charTyped(char c, int mods) {
            if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) return true;
            tf.charTyped(c, dblFilter()); return true;
        }
        @Override public void commit() {
            tf.clearSelection();
            try { setter.accept(Double.parseDouble(tf.value)); } catch (NumberFormatException ignored) {}
        }
        private static String trimDouble(double value) {
            String formatted = String.format("%.2f", value);
            if (formatted.contains(".")) formatted = formatted.replaceAll("0+$", "").replaceAll("\\.$", "");
            return formatted;
        }
    }

    static class ListInputSubPanel implements SubPanel {
        final String label;
        final StringBuilder value;
        final String defaultValue;
        final Consumer<List<String>> setter;
        final String[] hints;
        int w = -1;
        final int x, y, h = 122, screenW;
        boolean cursorVisible = true;
        long lastBlink = System.currentTimeMillis();
        int cursorIndex;
        int selStart = -1, selEnd = -1;
        int scrollLine = 0;

        boolean hasSelection() { return selStart >= 0 && selEnd >= 0 && selStart != selEnd; }
        int selMin() { return Math.min(selStart, selEnd); }
        int selMax() { return Math.max(selStart, selEnd); }
        void clearSel() { selStart = -1; selEnd = -1; }
        void deleteSelection() {
            if (!hasSelection()) return;
            int lo = selMin(), hi = selMax();
            value.delete(lo, hi);
            cursorIndex = lo;
            clearSel();
        }

        ListInputSubPanel(int mx, int my, int sw, int sh, String label, String defaultVal, List<String> initial, Consumer<List<String>> setter, String... hints) {
            this.label = label; this.setter = setter;
            this.hints = hints == null ? new String[0] : hints;
            String joined = initial == null ? "" : String.join("\n", initial);
            this.value = new StringBuilder(joined);
            this.defaultValue = defaultVal.isEmpty() ? joined : defaultVal;
            this.cursorIndex = this.value.length();
            this.screenW = sw; this.x = mx; this.y = Math.min(my, sh - h - 4);
        }

        private void ensureWidth(net.minecraft.client.gui.Font font) {
            if (w < 0) {
                int base = font.width(label) + 60;
                for (String hint : hints) base = Math.max(base, font.width(hint) + 20);
                w = Math.max(340, base);
            }
        }
        private int rx() { return Math.min(x, screenW - w - 4); }
        private int resetBtnX(net.minecraft.client.gui.Font font) { return rx() + w - font.width("reset") - 8; }
        private boolean overReset(int mx, int my, net.minecraft.client.gui.Font font) {
            int bx = resetBtnX(font);
            return mx >= bx - 2 && mx <= bx + font.width("reset") + 4 && my >= y + 3 && my <= y + 14;
        }

        private int textLeft() { return rx() + 6; }
        private int textTop()  { return y + 22 + hints.length * 10; }
        private int textRight() { return rx() + w - 6; }
        private int textBottom() { return y + h - 8; }
        private int lineStep(net.minecraft.client.gui.Font font) { return font.lineHeight + 1; }
        private List<String> lines() { return java.util.Arrays.asList(value.toString().split("\n", -1)); }

        private int cursorLine() {
            int line = 0;
            for (int i = 0; i < Math.min(cursorIndex, value.length()); i++) if (value.charAt(i) == '\n') line++;
            return line;
        }
        private int cursorColumn() {
            int col = 0;
            for (int i = Math.min(cursorIndex, value.length()) - 1; i >= 0; i--) { if (value.charAt(i) == '\n') break; col++; }
            return col;
        }
        private int lineStartIndex(int targetLine) {
            int line = 0;
            for (int i = 0; i < value.length(); i++) { if (line == targetLine) return i; if (value.charAt(i) == '\n') line++; }
            return line == targetLine ? value.length() : value.length();
        }
        private int lineEndIndex(int startIndex) { int idx = value.indexOf("\n", startIndex); return idx == -1 ? value.length() : idx; }
        private int visibleLineCount(net.minecraft.client.gui.Font font) { return Math.max(1, (textBottom() - textTop()) / lineStep(font)); }
        private int maxScrollLine(net.minecraft.client.gui.Font font) { return Math.max(0, lines().size() - visibleLineCount(font)); }
        private void clampScroll(net.minecraft.client.gui.Font font) { scrollLine = Math.max(0, Math.min(maxScrollLine(font), scrollLine)); }
        private void ensureCursorVisible(net.minecraft.client.gui.Font font) {
            int cl = cursorLine(), vis = visibleLineCount(font);
            if (cl < scrollLine) scrollLine = cl;
            else if (cl >= scrollLine + vis) scrollLine = cl - vis + 1;
            clampScroll(font);
        }
        private void moveCursorHorizontal(int delta) { cursorIndex = Math.max(0, Math.min(value.length(), cursorIndex + delta)); }
        private void moveCursorVertical(int delta) {
            int tl = Math.max(0, cursorLine() + delta), tc = cursorColumn();
            int ts = lineStartIndex(tl);
            cursorIndex = Math.min(ts + tc, lineEndIndex(ts));
        }
        private void insertText(String text) { if (text == null || text.isEmpty()) return; value.insert(cursorIndex, text); cursorIndex += text.length(); }

        @Override public void render(GuiGraphics g, int mx, int my, net.minecraft.client.gui.Font font) {
            ensureWidth(font);
            int rx = rx();
            fillRoundRect(g, rx - 2, y - 2, w + 4, h + 4, 4, C_SPBD());
            fillRoundRect(g, rx, y, w, h, 3, C_SPBG());
            MacroConfig.drawStyledText(g, font, label, rx + 5, y + 6, C_TXT());
            MacroConfig.drawStyledText(g, font, tr("reset", "重置"), resetBtnX(font), y + 4, overReset(mx, my, font) ? 0xFFFF5555 : C_DIM());
            int hintY = y + 16;
            for (String hint : hints) { MacroConfig.drawStyledText(g, font, hint, rx + 6, hintY, C_DIM()); hintY += 10; }
            g.fill(rx + 4, y + 18 + hints.length * 10, rx + w - 4, y + h - 6, C_SBGR());
            g.fill(rx + 4, y + h - 6, rx + w - 4, y + h - 5, C_ACC());
            if (System.currentTimeMillis() - lastBlink > 500) { cursorVisible = !cursorVisible; lastBlink = System.currentTimeMillis(); }
            clampScroll(font);
            List<String> lines = lines();
            int step = lineStep(font), drawY = textTop(), visible = visibleLineCount(font);
            int caretLine = cursorLine(), caretCol = cursorColumn();
            for (int li = scrollLine; li < lines.size() && li < scrollLine + visible; li++) {
                String line = lines.get(li);
                if (hasSelection()) {
                    int lo = selMin(), hi = selMax();
                    int lineStart = lineStartIndex(li);
                    int lineEnd = lineEndIndex(lineStart);
                    int slo = Math.max(lo, lineStart), shi = Math.min(hi, lineEnd);
                    if (slo < shi) {
                        String pre = value.substring(lineStart, slo);
                        String sel = value.substring(slo, shi);
                        int sx0 = textLeft() + font.width(pre);
                        int sx1 = sx0 + font.width(sel);
                        g.fill(Math.max(textLeft(), sx0), drawY, Math.min(textRight(), sx1), drawY + font.lineHeight, 0x664488FF);
                    }
                }
                MacroConfig.drawStyledText(g, font, line, textLeft(), drawY, C_TXT());
                if (cursorVisible && li == caretLine && !hasSelection()) {
                    int caretX = textLeft() + font.width(line.substring(0, Math.min(caretCol, line.length())));
                    g.fill(caretX, drawY - 1, caretX + 1, drawY + font.lineHeight, C_TXT());
                }
                drawY += step;
            }
            if (maxScrollLine(font) > 0) {
                int tx0 = rx + w - 5, tx1 = rx + w - 3, ty0 = y + 20 + hints.length * 10, ty1 = y + h - 8;
                int th = ty1 - ty0, total = lines.size(), thumbH = Math.max(10, th * visible / total);
                int travel = Math.max(0, th - thumbH), maxSc = maxScrollLine(font);
                int thumbY = ty0 + (maxSc == 0 ? 0 : travel * scrollLine / maxSc);
                g.fill(tx0, ty0, tx1, ty1, C_OFF());
                g.fill(tx0, thumbY, tx1, thumbY + thumbH, C_ACC());
            }
        }

        @Override public boolean contains(int mx, int my) {
            if (w < 0) return false;
            int rx = rx(); return mx >= rx - 2 && mx <= rx + w + 2 && my >= y - 2 && my <= y + h + 2;
        }
        @Override public boolean mouseClicked(int mx, int my, int btn, net.minecraft.client.gui.Font font) {
            if (overReset(mx, my, font)) {
                value.setLength(0); value.append(defaultValue); cursorIndex = value.length(); commit(); return true;
            }
            int rx = rx();
            if (mx >= rx + 4 && mx <= rx + w - 4 && my >= y + 18 + hints.length * 10 && my <= y + h - 6) {
                clampScroll(font);
                int line = scrollLine + Math.max(0, (my - textTop()) / Math.max(1, lineStep(font)));
                List<String> lines = lines();
                line = Math.max(0, Math.min(lines.size() - 1, line));
                String textLine = lines.get(line);
                int clickedX = Math.max(textLeft(), Math.min(textRight(), mx)), column = 0;
                for (int i = 1; i <= textLine.length(); i++) { if (textLeft() + font.width(textLine.substring(0, i)) > clickedX) break; column = i; }
                int ls = lineStartIndex(line);
                cursorIndex = Math.min(ls + column, ls + textLine.length());
                clearSel();
                ensureCursorVisible(font);
            }
            return true;
        }
        @Override public void scroll(int dir) {
            net.minecraft.client.gui.Font font = Minecraft.getInstance().font;
            scrollLine += dir; clampScroll(font);
        }
        @Override public boolean keyPressed(int key, int scan, int mods) {
            net.minecraft.client.gui.Font font = Minecraft.getInstance().font;
            if (key == GLFW.GLFW_KEY_SPACE) { if (hasSelection()) deleteSelection(); insertText(" "); ensureCursorVisible(font); return true; }
            if (key == 259) {
                if (hasSelection()) { deleteSelection(); ensureCursorVisible(font); }
                else if (cursorIndex > 0) { value.deleteCharAt(cursorIndex - 1); cursorIndex--; ensureCursorVisible(font); }
                return true;
            }
            if (key == 261) {
                if (hasSelection()) { deleteSelection(); ensureCursorVisible(font); }
                else if (cursorIndex < value.length()) { value.deleteCharAt(cursorIndex); ensureCursorVisible(font); }
                return true;
            }
            if (key == 257 || key == 335) {
                if ((mods & GLFW.GLFW_MOD_SHIFT) != 0) { commit(); }
                else { if (hasSelection()) deleteSelection(); insertText("\n"); ensureCursorVisible(font); }
                return true;
            }
            if (key == GLFW.GLFW_KEY_LEFT)  { int prev = cursorIndex; if ((mods & GLFW.GLFW_MOD_SHIFT) != 0) { if (!hasSelection()) selStart = prev; moveCursorHorizontal(-1); selEnd = cursorIndex; } else { clearSel(); moveCursorHorizontal(-1); } ensureCursorVisible(font); return true; }
            if (key == GLFW.GLFW_KEY_RIGHT) { int prev = cursorIndex; if ((mods & GLFW.GLFW_MOD_SHIFT) != 0) { if (!hasSelection()) selStart = prev; moveCursorHorizontal(1);  selEnd = cursorIndex; } else { clearSel(); moveCursorHorizontal(1);  } ensureCursorVisible(font); return true; }
            if (key == GLFW.GLFW_KEY_UP)    { int prev = cursorIndex; if ((mods & GLFW.GLFW_MOD_SHIFT) != 0) { if (!hasSelection()) selStart = prev; moveCursorVertical(-1);   selEnd = cursorIndex; } else { clearSel(); moveCursorVertical(-1);   } ensureCursorVisible(font); return true; }
            if (key == GLFW.GLFW_KEY_DOWN)  { int prev = cursorIndex; if ((mods & GLFW.GLFW_MOD_SHIFT) != 0) { if (!hasSelection()) selStart = prev; moveCursorVertical(1);    selEnd = cursorIndex; } else { clearSel(); moveCursorVertical(1);    } ensureCursorVisible(font); return true; }
            if (key == 67 && (mods & org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL) != 0) {
                String toCopy = hasSelection() ? value.substring(selMin(), selMax()) : value.toString();
                Minecraft.getInstance().keyboardHandler.setClipboard(toCopy); return true;
            }
            if (key == 86 && (mods & org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL) != 0) {
                if (hasSelection()) deleteSelection();
                String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
                if (clip != null) { insertText(clip.replace("\r", "")); ensureCursorVisible(font); } return true;
            }
            Character c = fallbackCharFromKey(key, scan, mods);
            if (c != null) { insertText(String.valueOf(c)); ensureCursorVisible(font); return true; }
            return false;
        }
        @Override public boolean charTyped(char c, int mods) {
            if (c == '\r') return true;
            if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) return true;
            if ((mods & GLFW.GLFW_MOD_SHIFT) != 0 && c >= 128) return true;
            if (hasSelection()) deleteSelection();
            insertText(String.valueOf(c)); ensureCursorVisible(Minecraft.getInstance().font); return true;
        }
        @Override public void commit() {
            clearSel();
            List<String> values = new ArrayList<>();
            for (String line : value.toString().split("\\R")) { String t = line.trim(); if (!t.isEmpty()) values.add(t); }
            setter.accept(values);
        }
    }
}
