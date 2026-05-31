/*
 * tco client — Discord Rich Presence (native discord-rpc, same stack as tcohack)
 */

package meteordevelopment.meteorclient.tco;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

public final class TcoDiscordRpc {
    private static final DiscordRPC LIB = DiscordRPC.INSTANCE;

    private static DiscordRichPresence presence;
    private static Thread callbackThread;
    private static volatile boolean running;

    private TcoDiscordRpc() {}

    public static synchronized void start(String appId, String largeImageKey, String largeImageText, String details) {
        stop();

        try {
            DiscordEventHandlers handlers = new DiscordEventHandlers();
            LIB.Discord_Initialize(appId, handlers, true, null);

            presence = new DiscordRichPresence();
            presence.startTimestamp = System.currentTimeMillis() / 1000L;
            presence.details = truncate(details, 128);
            presence.state = "In menu";
            presence.largeImageKey = truncate(largeImageKey, 256);
            presence.largeImageText = truncate(largeImageText, 128);

            LIB.Discord_UpdatePresence(presence);

            running = true;
            callbackThread = new Thread(() -> {
                while (running) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    LIB.Discord_RunCallbacks();
                }
            }, "tco-discord-rpc");
            callbackThread.setDaemon(true);
            callbackThread.start();

            MeteorClient.LOG.info("Discord RPC started (app {})", appId);
        } catch (Throwable error) {
            running = false;
            MeteorClient.LOG.error("Discord RPC failed to start", error);
            ChatUtils.error("Discord RPC failed: " + error.getMessage());
        }
    }

    public static synchronized void update(String details, String state) {
        if (!running || presence == null) return;

        presence.details = truncate(details, 128);
        presence.state = truncate(state, 128);
        LIB.Discord_UpdatePresence(presence);
    }

    public static synchronized void stop() {
        running = false;

        if (callbackThread != null) {
            callbackThread.interrupt();
            callbackThread = null;
        }

        try {
            LIB.Discord_ClearPresence();
            LIB.Discord_Shutdown();
        } catch (Throwable ignored) {}

        presence = null;
    }

    public static boolean isRunning() {
        return running;
    }

    private static String truncate(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, max);
    }
}
