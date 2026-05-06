package com.ihanuat.mod.modules;

import com.ihanuat.mod.MacroConfig;
import com.ihanuat.mod.MacroState;
import com.ihanuat.mod.MacroStateManager;
import com.ihanuat.mod.MacroWorkerThread;
import com.ihanuat.mod.mixin.AccessorInventory;
import com.ihanuat.mod.util.ClientUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Disco Destination Mode — manual-kill replacement for the Taunahi pest cleaner.
 *
 * <p>Behaviour:
 * <ol>
 *   <li>Pest detection / teleport / gear-restore are handled by the existing
 *       {@link PestCleaningSequencer} pipeline.</li>
 *   <li>Once the player has arrived on the infested plot, this manager equips
 *       the vacuum (if present in the hotbar) and continuously holds the
 *       {@code keyUse} (right-click) key. The key is held — not spam-clicked —
 *       so Minecraft's own {@code MultiPlayerGameMode} fires item-use ticks
 *       at the natural cooldown rate.</li>
 *   <li>The hold loop polls the existing pest-count sources (sidebar/tab list)
 *       via {@link PestManager}'s shared logic and exits as soon as the count
 *       drops to or below {@link MacroConfig#manualPestRewarpAt}, or when the
 *       hard timeout expires.</li>
 *   <li>Return-to-farm is delegated to the existing
 *       {@link PestManager#checkTabListForPests} → {@link PestReturnManager}
 *       flow, which already triggers when alive-count is at/below the rewarp
 *       threshold for the configured delay.</li>
 * </ol>
 *
 * <p>The disco hold task is queued on the shared {@link MacroWorkerThread},
 * but it always releases {@code keyUse} in a {@code finally} block — even on
 * abort, exception, or stop-macro — so the player is never stranded with a
 * pinned right-click.
 */
public class DiscoDestinationManager {

    /**
     * Hard upper bound on the time we'll keep right-click held before bailing
     * out and letting the standard return-to-farm path run anyway. Prevents
     * permanent stuck-state if pest detection silently fails.
     */
    private static final long HARD_TIMEOUT_MS = 90_000L;

    /** How often we poll pest counts inside the hold loop. */
    private static final long POLL_INTERVAL_MS = 250L;

    /**
     * Grace period at the start of the hold during which we don't yet trust
     * the "0 pests" reading — the sidebar/tab list lag a tick or two after
     * teleporting onto the plot, and we don't want to bail out before we've
     * actually started shooting.
     */
    private static final long INITIAL_GRACE_MS = 1500L;

    public static volatile boolean isActive = false;

    private static volatile int activeSessionId = 0;

    private DiscoDestinationManager() {}

    public static void resetState() {
        // Mark inactive so any in-flight hold loop self-exits at its next poll.
        isActive = false;
        activeSessionId++;
    }

    /**
     * Entry point — called by {@link PestCleaningSequencer} when
     * {@link MacroConfig#discoDestinationMode} is on, after gear/teleport
     * have completed and we're standing on the infested plot.
     */
    public static void start(Minecraft client, String infestedPlot, int pestSessionId) {
        if (client == null || client.player == null) return;
        if (isActive) {
            ClientUtils.sendDebugMessage(client,
                    "Disco Destination: start() ignored, already active.");
            return;
        }

        // Equip vacuum first. Fail gracefully if it isn't in the hotbar — the
        // pest cleaning state is left in place so the user can still rescue
        // the situation manually, and the standard return path will fire on
        // the no-pest timeout.
        int vacuumSlot = ClientUtils.findVacuumSlot(client);
        if (vacuumSlot < 0) {
            client.player.displayClientMessage(
                    Component.literal("§cDisco Destination: no Pest Vacuum in hotbar — aborting hold."),
                    false);
            ClientUtils.sendDebugMessage(client,
                    "Disco Destination: vacuum not found, refusing to start hold loop.");
            return;
        }

        final int targetSlot = vacuumSlot;
        client.execute(() -> {
            if (client.player == null) return;
            ((AccessorInventory) client.player.getInventory()).setSelected(targetSlot);
        });
        ClientUtils.sendDebugMessage(client,
                "Disco Destination: equipped vacuum in slot " + targetSlot + ".");

        isActive = true;
        final int mySessionId = ++activeSessionId;

        client.player.displayClientMessage(
                Component.literal("§dDisco Destination: holding right-click on plot " + infestedPlot + "..."),
                true);

        MacroWorkerThread.getInstance().submit("DiscoDestination-Hold-" + infestedPlot,
                () -> runHoldLoop(client, pestSessionId, mySessionId));
    }

    private static void runHoldLoop(Minecraft client, int pestSessionId, int mySessionId) {
        final long startMs = System.currentTimeMillis();
        boolean keyHeld = false;
        try {
            // Press and hold right-click. Minecraft's gameMode tick will fire
            // useItem at the configured cooldown rate while this stays down.
            client.execute(() -> {
                if (client.options != null) client.options.keyUse.setDown(true);
            });
            keyHeld = true;
            ClientUtils.sendDebugMessage(client, "Disco Destination: keyUse held down.");

            while (true) {
                if (!isActive || mySessionId != activeSessionId) {
                    ClientUtils.sendDebugMessage(client,
                            "Disco Destination: hold loop superseded or reset, exiting.");
                    return;
                }
                if (MacroWorkerThread.shouldAbortTask(client, MacroState.State.CLEANING)) {
                    ClientUtils.sendDebugMessage(client,
                            "Disco Destination: macro state changed, exiting hold loop.");
                    return;
                }
                if (pestSessionId != PestManager.currentPestSessionId) {
                    ClientUtils.sendDebugMessage(client,
                            "Disco Destination: pest session id changed, exiting hold loop.");
                    return;
                }

                long elapsed = System.currentTimeMillis() - startMs;

                if (elapsed > HARD_TIMEOUT_MS) {
                    client.player.displayClientMessage(
                            Component.literal("§cDisco Destination: hard timeout reached — releasing hold."),
                            false);
                    ClientUtils.sendDebugMessage(client,
                            "Disco Destination: hard timeout (" + HARD_TIMEOUT_MS + "ms) reached.");
                    return;
                }

                if (elapsed >= INITIAL_GRACE_MS) {
                    int aliveCount = readAliveCount(client);
                    if (aliveCount >= 0 && aliveCount <= MacroConfig.manualPestRewarpAt) {
                        ClientUtils.sendDebugMessage(client,
                                "Disco Destination: pest count " + aliveCount
                                        + " <= rewarpAt " + MacroConfig.manualPestRewarpAt
                                        + " — releasing hold; standard return flow will take over.");
                        return;
                    }
                }

                if (!MacroWorkerThread.sleep(POLL_INTERVAL_MS)) {
                    return; // interrupted
                }
            }
        } catch (Throwable t) {
            ClientUtils.sendDebugMessage(client,
                    "Disco Destination: hold loop threw " + t.getClass().getSimpleName()
                            + ": " + t.getMessage());
        } finally {
            if (keyHeld) {
                client.execute(() -> {
                    if (client.options != null) client.options.keyUse.setDown(false);
                });
                ClientUtils.sendDebugMessage(client, "Disco Destination: keyUse released.");
            }
            isActive = false;
            // Note: we deliberately do NOT call PestReturnManager.handlePestCleaningFinished
            // ourselves — PestManager.checkTabListForPests already arms manualReturn
            // and triggers the return after the configured delay when alive-count
            // is at/below the rewarp threshold. This keeps disco mode aligned with
            // the existing manual-clean return semantics.
        }
    }

    /**
     * Reads the current alive pest count, preferring the sidebar (which
     * matches what the player sees) and falling back to the tab list.
     * Returns -1 if neither source has a usable value yet.
     */
    private static int readAliveCount(Minecraft client) {
        int sidebar = ClientUtils.getGardenPestCountFromSidebar(client);
        if (sidebar >= 0) return sidebar;

        PestTabListParser.TabListData tab = PestTabListParser.parseTabList(client);
        return tab.aliveCount; // -1 if tab not yet populated
    }

    /**
     * Emergency hook — drops the held key immediately. Safe to call from any
     * thread / from teardown paths. Idempotent.
     */
    public static void forceRelease(Minecraft client) {
        isActive = false;
        if (client == null) return;
        client.execute(() -> {
            if (client.options != null) client.options.keyUse.setDown(false);
        });
    }

    /**
     * Convenience helper — true when disco is actively holding right-click.
     * Other modules can use this to avoid stomping on the input state.
     */
    public static boolean isHolding() {
        return isActive && MacroStateManager.getCurrentState() == MacroState.State.CLEANING;
    }
}
