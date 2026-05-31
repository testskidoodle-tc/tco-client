/*
 * tco client — title screen music from bundled assets
 */

package meteordevelopment.meteorclient.tco;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public final class TitleScreenMusic {
    private static final Identifier SOUND_ID = Identifier.fromNamespaceAndPath(MeteorClient.MOD_ID, "tco.title_theme");
    private static boolean started;

    private TitleScreenMusic() {}

    public static void play() {
        if (started || mc == null) return;
        started = true;

        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(SOUND_ID);
        if (sound == null) {
            MeteorClient.LOG.warn("Title music not loaded ({}). Add assets or run scripts/fetch-title-music.ps1", SOUND_ID);
            return;
        }

        mc.getSoundManager().play(SimpleSoundInstance.forMusic(sound));
        MeteorClient.LOG.info("Playing tco client title music");
    }

    public static void stop() {
        if (!started || mc == null) return;
        mc.getSoundManager().stop(SOUND_ID, SoundSource.MUSIC);
        started = false;
    }
}
