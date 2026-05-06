package com.ihanuat.mod;

import com.ihanuat.mod.modules.TodayTimeTracker;
import net.minecraft.client.Minecraft;
import com.ihanuat.mod.util.ClientUtils;

public class MacroStateManager {
    private static volatile MacroState.State currentState = MacroState.State.OFF;
    private static volatile boolean intentionalDisconnect = false;
    private static volatile long sessionAccumulated = 0;
    private static volatile long lifetimeAccumulated = 0;
    private static volatile long lastSessionStartTime = 0;
    private static long lastPeriodicSaveTime = 0;

    public static void resetSession() {
        if (isMacroRunning()) {
            lastSessionStartTime = System.currentTimeMillis();
        } else {
            lastSessionStartTime = 0;
        }
        sessionAccumulated = 0;
        com.ihanuat.mod.modules.profitTracker.ProfitManager.reset();
        com.ihanuat.mod.modules.DynamicRestManager.reset();
    }

    public static void syncFromConfig() {
        lifetimeAccumulated = MacroConfig.lifetimeAccumulated;
        TodayTimeTracker.syncFromConfig();
        com.ihanuat.mod.modules.QuitThresholdManager.syncFromConfig();
    }

    public static void periodicUpdate() {
        // Tick today tracker for day-rollover detection
        TodayTimeTracker.tick();
        if (currentState == MacroState.State.OFF || currentState == MacroState.State.RECOVERING) return;

        long now = System.currentTimeMillis();
        if (lastSessionStartTime <= 0) {
            lastPeriodicSaveTime = now;
            return;
        }
        if (now - lastPeriodicSaveTime > 60_000) {
            lastPeriodicSaveTime = now;
            long diff = Math.max(0L, now - lastSessionStartTime);
            MacroConfig.lifetimeAccumulated = lifetimeAccumulated + diff;
            MacroConfig.save();
        }
    }

    public static long getSessionRunningTime() {
        if (currentState != MacroState.State.OFF && currentState != MacroState.State.RECOVERING
                && lastSessionStartTime != 0) {
            return sessionAccumulated + (System.currentTimeMillis() - lastSessionStartTime);
        }
        return sessionAccumulated;
    }

    public static long getLifetimeRunningTime() {
        if (currentState != MacroState.State.OFF && currentState != MacroState.State.RECOVERING
                && lastSessionStartTime != 0) {
            return lifetimeAccumulated + (System.currentTimeMillis() - lastSessionStartTime);
        }
        return lifetimeAccumulated;
    }

    public static boolean isMacroRunning() {
        return currentState != MacroState.State.OFF;
    }

    public static long getMacroActiveSinceMs() {
        if (currentState == MacroState.State.OFF) return 0L;
        return lastSessionStartTime;
    }

    public static boolean isIntentionalDisconnect() { return intentionalDisconnect; }
    public static void setIntentionalDisconnect(boolean v) { intentionalDisconnect = v; }
    public static MacroState.State getCurrentState() { return currentState; }

    public static void setCurrentState(MacroState.State state) {
        MacroState.State prev = currentState;
        boolean wasActive = prev != MacroState.State.OFF && prev != MacroState.State.RECOVERING;
        boolean willActive = state != MacroState.State.OFF && state != MacroState.State.RECOVERING;

        if (prev == MacroState.State.OFF && willActive) {
            lastSessionStartTime = System.currentTimeMillis();
            if (!MacroConfig.persistSessionTimer) {
                sessionAccumulated = 0;
                com.ihanuat.mod.modules.profitTracker.ProfitManager.reset();
            }
            lastPeriodicSaveTime = System.currentTimeMillis();
            TodayTimeTracker.onMacroStart();
            com.ihanuat.mod.modules.QuitThresholdManager.onMacroStart();

        } else if (prev == MacroState.State.RECOVERING && willActive) {
            lastSessionStartTime = System.currentTimeMillis();
            TodayTimeTracker.onMacroStart();
            com.ihanuat.mod.modules.QuitThresholdManager.onMacroStart();

        } else if (wasActive && !willActive) {
            if (lastSessionStartTime != 0) {
                long diff = System.currentTimeMillis() - lastSessionStartTime;
                sessionAccumulated += diff;
                lifetimeAccumulated += diff;
                lastSessionStartTime = 0;
                MacroConfig.lifetimeAccumulated = lifetimeAccumulated;
                MacroConfig.save();
            }
            TodayTimeTracker.onMacroPause();
            com.ihanuat.mod.modules.QuitThresholdManager.onMacroPause();
            // Pause the dynamic-rest countdown so it doesn't tick while macro is off
            com.ihanuat.mod.modules.DynamicRestManager.pauseTimer();
        }

        currentState = state;
    }

    public static void stopMacro(Minecraft client) {
        setCurrentState(MacroState.State.OFF);
        MacroWorkerThread.getInstance().cancelCurrent();
        if (client != null) {
            client.execute(() -> { if (client.screen != null) client.setScreen(null); });
        }
        com.ihanuat.mod.modules.DiscoDestinationManager.forceRelease(client);
        ClientUtils.forceReleaseKeys(client);
        ClientUtils.sendDebugMessage(client, "Stopping script: Macro stopped by user");
        com.ihanuat.mod.util.CommandUtils.stopScript(client, 0);
        com.ihanuat.mod.modules.PestManager.reset();
        com.ihanuat.mod.modules.GearManager.reset();
        com.ihanuat.mod.modules.GeorgeManager.reset();
        com.ihanuat.mod.modules.BookCombineManager.reset();
        com.ihanuat.mod.modules.JunkManager.reset();
        com.ihanuat.mod.modules.RecoveryManager.reset();
        if (!MacroConfig.persistSessionTimer) {
            com.ihanuat.mod.modules.DynamicRestManager.reset();
            com.ihanuat.mod.modules.profitTracker.ProfitManager.reset();
        }
        // pauseTimer already called by setCurrentState(OFF) above when persistSessionTimer=true
        ReconnectScheduler.cancel();
    }
}
