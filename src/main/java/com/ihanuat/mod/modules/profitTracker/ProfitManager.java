package com.ihanuat.mod.modules.profitTracker;

import com.ihanuat.mod.MacroConfig;
import com.ihanuat.mod.MacroState;
import com.ihanuat.mod.MacroStateManager;
import com.ihanuat.mod.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Core profit tracker that coordinates session/daily/lifetime counting,
 * HUD snapshot management, persistence, and delegates to helper modules:
 * {@link BazaarService}, {@link ChatParser}, {@link InventoryTracker},
 * {@link PetXpTracker}, {@link SackTracker}.
 */
public class ProfitManager {

    // ── Data Maps ────────────────────────────────────────────────────────────
    private static final Map<String, Long> sessionCounts = new LinkedHashMap<>();
    private static final Map<String, Long> dailyCounts = new LinkedHashMap<>();
    private static final Map<String, Long> lifetimeCounts = new LinkedHashMap<>();

    // ── HUD Snapshot Cache ───────────────────────────────────────────────────
    private static final Map<String, ProfitHudSnapshot> profitHudSnapshots = new java.util.HashMap<>();
    private static final Set<String> dirtyHudModes = new HashSet<>(Arrays.asList("session", "daily", "lifetime"));

    // ── Spray tracking ───────────────────────────────────────────────────────
    private static long spraySessionQuantity = 0;
    private static long sprayDailyQuantity = 0;
    private static long sprayLifetimeQuantity = 0;
    public static volatile boolean isSprayPhaseActive = false;

    // ── Daily reset ──────────────────────────────────────────────────────────
    private static String lastDailyResetDate = getCurrentDateString();

    // ── Persistence ──────────────────────────────────────────────────────────
    private static final java.io.File LIFETIME_FILE = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir()
            .resolve("ihanuat_profit_lifetime.json").toFile();
    private static final java.io.File DAILY_FILE = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir()
            .resolve("ihanuat_profit_daily.json").toFile();
    private static final com.google.gson.Gson GSON = new com.google.gson.GsonBuilder().setPrettyPrinting().create();

    // ── Spray Phase ──────────────────────────────────────────────────────────

    public static void startSprayPhase() {
        if (!isSprayPhaseActive) {
            isSprayPhaseActive = true;
        }
    }

    public static void stopSprayPhase() {
        if (isSprayPhaseActive) {
            isSprayPhaseActive = false;
        }
    }

    // ── HUD Snapshot ─────────────────────────────────────────────────────────

    public static final class ProfitHudSnapshot {
        private final Map<String, Long> activeDrops;
        private final Map<String, Long> compactDrops;
        private final long totalProfit;
        private final int activeItemCount;
        private final int compactNonZeroCount;

        private ProfitHudSnapshot(Map<String, Long> activeDrops, Map<String, Long> compactDrops, long totalProfit) {
            this.activeDrops = java.util.Collections.unmodifiableMap(activeDrops);
            this.compactDrops = java.util.Collections.unmodifiableMap(compactDrops);
            this.totalProfit = totalProfit;
            this.activeItemCount = activeDrops.size();
            this.compactNonZeroCount = (int) compactDrops.values().stream().filter(value -> value != 0).count();
        }

        public Map<String, Long> activeDrops() {
            return activeDrops;
        }

        public Map<String, Long> compactDrops() {
            return compactDrops;
        }

        public long totalProfit() {
            return totalProfit;
        }

        public int activeItemCount() {
            return activeItemCount;
        }

        public int compactNonZeroCount() {
            return compactNonZeroCount;
        }
    }

    static void markHudDirty(String... modes) {
        synchronized (profitHudSnapshots) {
            for (String mode : modes) {
                dirtyHudModes.add(mode);
            }
        }
    }

    static void markAllHudDirty() {
        markHudDirty("session", "daily", "lifetime");
    }

    private static Map<String, Long> getCountsForMode(String mode) {
        if ("daily".equals(mode)) {
            return dailyCounts;
        }
        if ("lifetime".equals(mode)) {
            return lifetimeCounts;
        }
        return sessionCounts;
    }

    private static Map<String, Long> buildCompactDrops(Map<String, Long> targetCounts) {
        Map<String, Long> compact = new LinkedHashMap<>();
        compact.put("Crops", 0L);
        compact.put("Pest Items", 0L);
        compact.put("Pets", 0L);
        compact.put("Misc Drops", 0L);
        compact.put("Feast", 0L);
        compact.put("Visitor", 0L);
        compact.put("Costs", 0L);
        compact.put("Others", 0L);

        boolean isCleaning = MacroStateManager.getCurrentState() == MacroState.State.CLEANING;

        for (Map.Entry<String, Long> entry : targetCounts.entrySet()) {
            String name = entry.getKey();
            long count = entry.getValue();
            long profit = (long) (BazaarService.getItemPrice(name) * count);

            if (name.equals("[Visitor] Visitor Cost") || name.equals("[Spray] Sprayonator") || profit < 0) {
                compact.put("Costs", compact.get("Costs") + profit);
            } else if (ItemConstants.CROPS_SET.contains(name)) {
                if (isCleaning) {
                    compact.put("Pest Items", compact.get("Pest Items") + profit);
                } else {
                    compact.put("Crops", compact.get("Crops") + profit);
                }
            } else if (ItemConstants.PEST_ITEMS_SET.contains(name)) {
                compact.put("Pest Items", compact.get("Pest Items") + profit);
            } else if (ItemConstants.PETS_SET.contains(name)) {
                compact.put("Pets", compact.get("Pets") + profit);
            } else if (ItemConstants.FEAST_ITEMS_SET.contains(name)) {
                compact.put("Feast", compact.get("Feast") + profit);
            } else if (ItemConstants.MISC_DROPS_SET.contains(name) || name.toLowerCase().startsWith("pet xp (")) {
                if (isCleaning) {
                    compact.put("Pest Items", compact.get("Pest Items") + profit);
                } else {
                    compact.put("Misc Drops", compact.get("Misc Drops") + profit);
                }
            } else if (name.startsWith("[Visitor] ")) {
                compact.put("Visitor", compact.get("Visitor") + profit);
            } else {
                compact.put("Others", compact.get("Others") + profit);
            }
        }

        return compact.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1,
                        LinkedHashMap::new));
    }

    private static ProfitHudSnapshot rebuildHudSnapshot(String mode) {
        Map<String, Long> targetCounts = getCountsForMode(mode);
        Map<String, Long> activeDrops = targetCounts.entrySet().stream()
                .sorted((e1, e2) -> {
                    double p1 = BazaarService.getItemPrice(e1.getKey()) * e1.getValue();
                    double p2 = BazaarService.getItemPrice(e2.getKey()) * e2.getValue();
                    return Double.compare(p2, p1);
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1,
                        LinkedHashMap::new));

        long totalProfit = 0L;
        for (Map.Entry<String, Long> entry : targetCounts.entrySet()) {
            totalProfit += (long) (BazaarService.getItemPrice(entry.getKey()) * entry.getValue());
        }

        return new ProfitHudSnapshot(activeDrops, buildCompactDrops(targetCounts), totalProfit);
    }

    public static ProfitHudSnapshot getHudSnapshot(String mode) {
        synchronized (profitHudSnapshots) {
            ProfitHudSnapshot snapshot = profitHudSnapshots.get(mode);
            if (snapshot == null || dirtyHudModes.contains(mode)) {
                snapshot = rebuildHudSnapshot(mode);
                profitHudSnapshots.put(mode, snapshot);
                dirtyHudModes.remove(mode);
            }
            return snapshot;
        }
    }

    // ── Chat Delegation ──────────────────────────────────────────────────────

    public static String formatChineseCoins(long value) {
        if (value < 0) return "-" + formatChineseCoins(-value);
        if (value >= 100_000_000L) {
            String s = String.format("%.1f亿", value / 100_000_000.0);
            return s.endsWith(".0亿") ? s.replace(".0亿", "亿") : s;
        }
        if (value >= 10_000L) {
            String s = String.format("%.1f万", value / 10_000.0);
            return s.endsWith(".0万") ? s.replace(".0万", "万") : s;
        }
        return String.format("%,d", value);
    }

    public static String formatCoins(long value) {
        if (value < 0) return "-" + formatCoins(-value);
        if (value >= 1_000_000_000L) {
            String s = String.format(Locale.US, "%.1fb", value / 1_000_000_000.0);
            return s.endsWith(".0b") ? s.replace(".0b", "b") : s;
        }
        if (value >= 1_000_000L) {
            String s = String.format(Locale.US, "%.1fm", value / 1_000_000.0);
            return s.endsWith(".0m") ? s.replace(".0m", "m") : s;
        }
        if (value >= 1_000L) {
            String s = String.format(Locale.US, "%.1fk", value / 1_000.0);
            return s.endsWith(".0k") ? s.replace(".0k", "k") : s;
        }
        return String.format(Locale.US, "%,d", value);
    }

    public static void handleChatMessage(Component component) {
        ChatParser.handleChatMessage(component);
    }

    // ── Auto-sell filter ─────────────────────────────────────────────────────

    /**
     * Returns true if this item name matches an entry in the Booster Cookie autosell list.
     * Such items should NOT be tracked as pest/drop entries -- they will be captured via
     * the purse increase when they are auto-sold through the cookie GUI, preventing double-counting.
     */
    private static boolean isAutoSellItem(String name) {
        if (name == null || MacroConfig.boosterCookieItems == null) return false;
        String lower = name.toLowerCase();
        for (String target : MacroConfig.boosterCookieItems) {
            if (target != null && !target.isBlank() && lower.contains(target.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    // ── Drop / Cost Tracking ─────────────────────────────────────────────────

    public static void addDrop(String itemName, long count) {
        // Handle items with suffix counts like "Mutant Nether Wart X9"
        String processedName = ClientUtils.stripColor(itemName).trim();
        long multiplier = 1;

        Matcher suffixMatcher = Pattern.compile("\\s+[xX](\\d+)$").matcher(processedName);
        if (suffixMatcher.find()) {
            try {
                multiplier = Long.parseLong(suffixMatcher.group(1));
                processedName = processedName.substring(0, suffixMatcher.start()).trim();
            } catch (Exception ignored) {
            }
        }

        long finalCount = count * multiplier;

        // Group all Vinyl items together
        if (processedName.toLowerCase().endsWith("vinyl")) {
            processedName = "Vinyl";
        }

        // Find the tracked item name that matches (case-insensitive) for pretty formatting
        String matchedName = null;
        for (String tracked : ItemConstants.TRACKED_ITEMS.keySet()) {
            if (tracked.equalsIgnoreCase(processedName)) {
                matchedName = tracked;
                break;
            }
        }

        if (matchedName == null) {
            if (processedName.toLowerCase().startsWith("pet xp (")) {
                matchedName = processedName;
            } else {
                matchedName = normalizeName(processedName);
            }
        }

        // Items in the autosell list are captured via purse increase on sale --
        // skip drop-category tracking here to prevent double-counting.
        if (isAutoSellItem(matchedName)) return;

        // Check for daily reset
        checkDailyReset();

        // Only add to session counts if macro is running
        if (MacroStateManager.isMacroRunning()) {
            sessionCounts.put(matchedName, sessionCounts.getOrDefault(matchedName, 0L) + finalCount);
        }

        dailyCounts.put(matchedName, dailyCounts.getOrDefault(matchedName, 0L) + finalCount);
        lifetimeCounts.put(matchedName, lifetimeCounts.getOrDefault(matchedName, 0L) + finalCount);
        markHudDirty("session", "daily", "lifetime");
        saveLifetime();
        saveDaily();
    }

    public static void addVisitorGain(String itemName, long count) {
        String cleanName = ClientUtils.stripColor(itemName).replace("+", "").trim();
        long multiplier = 1;
        Matcher m = Pattern.compile("\\s+[xX](\\d+)$").matcher(cleanName);
        if (m.find()) {
            try {
                multiplier = Long.parseLong(m.group(1));
                cleanName = cleanName.substring(0, m.start()).trim();
            } catch (Exception ignored) {
            }
        }

        String key = cleanName.startsWith("[Visitor] ") ? cleanName : "[Visitor] " + cleanName;
        long totalCount = count * multiplier;

        checkDailyReset();

        if (MacroStateManager.isMacroRunning()) {
            sessionCounts.put(key, sessionCounts.getOrDefault(key, 0L) + totalCount);
        }
        dailyCounts.put(key, dailyCounts.getOrDefault(key, 0L) + totalCount);
        lifetimeCounts.put(key, lifetimeCounts.getOrDefault(key, 0L) + totalCount);
        markHudDirty("session", "daily", "lifetime");
        saveLifetime();
        saveDaily();
    }

    public static void addVisitorCost(long coinsSpent) {
        String key = "[Visitor] Visitor Cost";
        checkDailyReset();
        sessionCounts.put(key, sessionCounts.getOrDefault(key, 0L) - coinsSpent);
        dailyCounts.put(key, dailyCounts.getOrDefault(key, 0L) - coinsSpent);
        lifetimeCounts.put(key, lifetimeCounts.getOrDefault(key, 0L) - coinsSpent);
        markHudDirty("session", "daily", "lifetime");
        saveLifetime();
        saveDaily();
    }

    public static void addSprayCost(int quantity, long coins) {
        String key = "[Spray] Sprayonator";
        checkDailyReset();
        spraySessionQuantity += quantity;
        sprayDailyQuantity += quantity;
        sprayLifetimeQuantity += quantity;
        if (MacroStateManager.isMacroRunning()) {
            sessionCounts.put(key, sessionCounts.getOrDefault(key, 0L) - coins);
        }
        dailyCounts.put(key, dailyCounts.getOrDefault(key, 0L) - coins);
        lifetimeCounts.put(key, lifetimeCounts.getOrDefault(key, 0L) - coins);
        markHudDirty("session", "daily", "lifetime");
        saveLifetime();
        saveDaily();
    }

    /**
     * Called by {@link PetXpTracker} to record XP gained this tick.
     */
    public static void addPetXp(String petName, long xpAmount) {
        if (xpAmount <= 0)
            return;
        addDrop("Pet XP (" + petName + ")", xpAmount);
    }

    // ── Spray Quantity Getters ───────────────────────────────────────────────

    public static long getSprayQuantity(boolean lifetime) {
        return lifetime ? sprayLifetimeQuantity : spraySessionQuantity;
    }

    public static long getSprayQuantity(String mode) {
        if ("daily".equals(mode)) return sprayDailyQuantity;
        if ("lifetime".equals(mode)) return sprayLifetimeQuantity;
        return spraySessionQuantity;
    }

    // ── Display Helpers ──────────────────────────────────────────────────────

    public static String getCategorizedName(String name) {
        if (name.equals("[Spray] Sprayonator")) {
            return "\u00a7c\u00a7l[COST] \u00a7fSprayonator";
        }
        if (name.equals("[Visitor] Visitor Cost")) {
            return "\u00a7c\u00a7l[COST] \u00a7fVisitor Cost";
        }
        if (name.startsWith("[Visitor] ")) {
            return "\u00a75\u00a7l[VISITOR] \u00a7f" + name.substring(10);
        }
        String color = "\u00a77";
        String tag = "OTHER";

        if (ItemConstants.CROPS_SET.contains(name)) {
            color = "\u00a7a";
            tag = "CROP";
        } else if (ItemConstants.PEST_ITEMS_SET.contains(name)) {
            color = "\u00a7d";
            tag = "PEST";
        } else if (ItemConstants.PETS_SET.contains(name)) {
            color = "\u00a76";
            tag = "PET";
        } else if (ItemConstants.FEAST_ITEMS_SET.contains(name)) {
            color = "\u00a7e";
            tag = "FEAST";
        } else if (ItemConstants.MISC_DROPS_SET.contains(name) || name.toLowerCase().startsWith("pet xp (")) {
            color = "\u00a7b";
            tag = "MISC";
        }

        String displayName = name.replace("Enchanted ", "Ench. ");
        if (name.toLowerCase().startsWith("pet xp (")) {
            displayName = name.substring(8, name.length() - 1) + " XP";
        }
        return color + "\u00a7l[" + tag + "] \u00a7f" + displayName;
    }

    public static String getCompactCategoryLabel(String category) {
        switch (category) {
            case "Crops":
                return "\u00a7a\u00a7l[CROP]";
            case "Pest Items":
                return "\u00a7d\u00a7l[PEST]";
            case "Pets":
                return "\u00a76\u00a7l[PET]";
            case "Misc Drops":
                return "\u00a7b\u00a7l[MISC]";
            case "Feast":
                return "\u00a7e\u00a7l[FEAST]";
            case "Visitor":
                return "\u00a75\u00a7l[VISITOR]";
            case "Costs":
                return "\u00a7c\u00a7l[COST]";
            default:
                return "\u00a77\u00a7l[OTHER]";
        }
    }

    private static String normalizeName(String name) {
        if (name == null || name.isEmpty())
            return "Unknown Item";

        StringBuilder b = new StringBuilder();
        boolean nextUpper = true;
        for (char c : name.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextUpper = true;
                b.append(c);
            } else if (nextUpper) {
                b.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                b.append(Character.toLowerCase(c));
            }
        }
        return b.toString();
    }

    // ── Public Queries ───────────────────────────────────────────────────────

    public static Map<String, Long> getActiveDrops() {
        return getActiveDrops("session");
    }

    public static Map<String, Long> getActiveDrops(boolean lifetime) {
        return getActiveDrops(lifetime ? "lifetime" : "session");
    }

    public static Map<String, Long> getActiveDrops(String mode) {
        return getHudSnapshot(mode).activeDrops();
    }

    public static Map<String, Long> getCompactDrops() {
        return getCompactDrops("session");
    }

    public static Map<String, Long> getCompactDrops(boolean lifetime) {
        return getCompactDrops(lifetime ? "lifetime" : "session");
    }

    public static Map<String, Long> getCompactDrops(String mode) {
        return getHudSnapshot(mode).compactDrops();
    }

    public static long getTotalProfit() {
        return getTotalProfit("session");
    }

    public static long getTotalProfit(boolean lifetime) {
        return getTotalProfit(lifetime ? "lifetime" : "session");
    }

    public static long getTotalProfit(String mode) {
        return getHudSnapshot(mode).totalProfit();
    }

    public static double getItemPrice(String itemName) {
        return BazaarService.getItemPrice(itemName);
    }

    public static boolean isPredefinedTrackedItem(String itemName) {
        if (itemName == null)
            return false;
        if (itemName.toLowerCase().startsWith("pet xp ("))
            return true;
        for (String tracked : ItemConstants.TRACKED_ITEMS.keySet()) {
            if (tracked.equalsIgnoreCase(itemName)) {
                return true;
            }
        }
        return false;
    }

    public static String fetchIdByName(String name) {
        return BazaarService.fetchIdByName(name);
    }

    public static void startStartupPriceFetch() {
        BazaarService.startStartupPriceFetch();
    }

    public static synchronized void refreshConfiguredPetXpPrices() {
        BazaarService.refreshConfiguredPetXpPrices();
    }

    public static void printPetXpPriceDebug(Minecraft client) {
        BazaarService.printPetXpPriceDebug(client);
    }

    // ── Reset ────────────────────────────────────────────────────────────────

    public static void reset() {
        sessionCounts.clear();
        spraySessionQuantity = 0;
        PetXpTracker.reset();
        InventoryTracker.reset();
        ChatParser.setLastBazaarSprayBuyTime(0);
        markHudDirty("session");
    }

    public static void resetDaily() {
        dailyCounts.clear();
        sprayDailyQuantity = 0;
        lastDailyResetDate = getCurrentDateString();
        markHudDirty("daily");
        saveDaily();
    }

    public static void resetLifetime() {
        lifetimeCounts.clear();
        markHudDirty("lifetime");
        saveLifetime();
    }

    // ── Main Update Loop ─────────────────────────────────────────────────────

    public static void update(Minecraft client) {
        if (client.player == null)
            return;

        if (MacroStateManager.getCurrentState() == MacroState.State.OFF) {
            BazaarService.checkAndRefresh();
            return;
        }

        InventoryTracker.update(client);
        PetXpTracker.update(client);
        BazaarService.checkAndRefresh();
    }

    // ── Persistence ──────────────────────────────────────────────────────────

    private static void saveLifetime() {
        try (java.io.FileWriter writer = new java.io.FileWriter(LIFETIME_FILE)) {
            GSON.toJson(lifetimeCounts, writer);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadLifetime() {
        if (!LIFETIME_FILE.exists())
            return;
        try (java.io.FileReader reader = new java.io.FileReader(LIFETIME_FILE)) {
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<Map<String, Long>>() {
            }.getType();
            Map<String, Long> data = GSON.fromJson(reader, type);
            if (data != null) {
                lifetimeCounts.clear();
                lifetimeCounts.putAll(data);
                markHudDirty("lifetime");
            }
        } catch (Exception e) {
            System.err.println("[Ihanuat] Failed to load lifetime profit data: " + e.getMessage());
        }
    }

    private static void saveDaily() {
        try (java.io.FileWriter writer = new java.io.FileWriter(DAILY_FILE)) {
            DailyData data = new DailyData();
            data.counts = new LinkedHashMap<>(dailyCounts);
            data.sprayQuantity = sprayDailyQuantity;
            data.resetDate = lastDailyResetDate;
            GSON.toJson(data, writer);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadDaily() {
        if (!DAILY_FILE.exists())
            return;
        try (java.io.FileReader reader = new java.io.FileReader(DAILY_FILE)) {
            DailyData data = GSON.fromJson(reader, DailyData.class);
            if (data != null) {
                lastDailyResetDate = data.resetDate != null ? data.resetDate : getCurrentDateString();

                // Check if we need to reset for a new day
                if (!lastDailyResetDate.equals(getCurrentDateString())) {
                    resetDaily();
                } else {
                    dailyCounts.clear();
                    if (data.counts != null) {
                        dailyCounts.putAll(data.counts);
                    }
                    sprayDailyQuantity = data.sprayQuantity;
                    markHudDirty("daily");
                }
            }
        } catch (Exception e) {
            System.err.println("[Ihanuat] Failed to load daily profit data: " + e.getMessage());
        }
    }

    /**
     * Gets the current date in YYYY-MM-DD format using the system's local timezone.
     */
    private static String getCurrentDateString() {
        java.time.LocalDate today = java.time.LocalDate.now();
        return today.toString();
    }

    /**
     * Checks if a new day has started and resets daily counts if needed.
     */
    private static void checkDailyReset() {
        String currentDate = getCurrentDateString();
        if (!currentDate.equals(lastDailyResetDate)) {
            resetDaily();
        }
    }

    private static class DailyData {
        Map<String, Long> counts;
        long sprayQuantity;
        String resetDate;
    }
}
