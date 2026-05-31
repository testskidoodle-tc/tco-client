/*
 * tco client — boot splash, title music, branding hooks
 */

package meteordevelopment.meteorclient.tco;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.DiscordPresence;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

public class TcoClientFeatures {
    private static final TcoClientFeatures INSTANCE = new TcoClientFeatures();

    private TcoClientFeatures() {}

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(INSTANCE);
        MeteorClient.LOG.info("tco client features loaded");
    }

    @PostInit
    public static void ensureDiscordRpc() {
        DiscordPresence rpc = Modules.get().get(DiscordPresence.class);
        if (rpc != null && !rpc.isActive()) {
            rpc.enable();
            MeteorClient.LOG.info("Enabled discord-presence module for tco client");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onOpenScreen(OpenScreenEvent event) {
        Screen screen = event.screen;
        if (screen == null) return;

        if (!TcoBoot.isBootComplete() && screen instanceof TitleScreen) {
            event.screen = new XpBootScreen();
            MeteorClient.LOG.info("Showing tco OS xp boot splash");
            return;
        }

        if (!(screen instanceof TitleScreen)) {
            TitleScreenMusic.stop();
        }
    }
}
