/*
 * tco client — title screen music from bundled assets
 */

package meteordevelopment.meteorclient.tco;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public final class TitleScreenMusic {
    private static final Identifier SOUND_ID = Identifier.fromNamespaceAndPath(MeteorClient.MOD_ID, "tco.title_theme");
    private static TcoTitleMusicSound current;

    private TitleScreenMusic() {}

    public static void play() {
        if (mc == null) return;

        stop();

        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(SOUND_ID);
        if (sound == null) {
            MeteorClient.LOG.warn("Title music not loaded ({})", SOUND_ID);
            return;
        }

        try {
            mc.getMusicManager().stopPlaying();
        } catch (Exception ignored) {}

        current = new TcoTitleMusicSound(sound);
        mc.getSoundManager().play(current);
        MeteorClient.LOG.info("Playing tco client title music (looping)");
    }

    public static void stop() {
        if (mc == null) return;

        if (current != null) {
            mc.getSoundManager().stop(current);
            current = null;
        }

        mc.getSoundManager().stop(SOUND_ID, SoundSource.MUSIC);
    }
}
