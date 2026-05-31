/*
 * tco client — title screen audio (extracted from YouTube video when available)
 */

package meteordevelopment.meteorclient.tco;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.nio.file.Files;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public final class TitleScreenMusic {
    private static final Identifier FALLBACK_SOUND = Identifier.fromNamespaceAndPath(MeteorClient.MOD_ID, "tco.title_theme");

    private static Clip wavClip;
    private static TcoTitleMusicSound mcSound;
    private static boolean started;

    private TitleScreenMusic() {}

    public static void play() {
        if (started || mc == null) return;

        stop();

        TcoMediaCache.ensureAsync(() -> mc.execute(TitleScreenMusic::playInternal));
    }

    private static void playInternal() {
        if (started || mc == null) return;
        started = true;

        try {
            mc.getMusicManager().stopPlaying();
        } catch (Exception ignored) {}

        if (Files.isRegularFile(TcoMediaCache.AUDIO_WAV)) {
            MeteorExecutor.execute(TitleScreenMusic::playWav);
            return;
        }

        playBundledFallback();
    }

    private static void playWav() {
        try {
            wavClip = AudioSystem.getClip();
            wavClip.open(AudioSystem.getAudioInputStream(TcoMediaCache.AUDIO_WAV.toFile()));
            wavClip.loop(Clip.LOOP_CONTINUOUSLY);
            wavClip.start();
            MeteorClient.LOG.info("Playing title audio from video (WAV)");
        } catch (Exception e) {
            MeteorClient.LOG.error("WAV title audio failed, using bundled fallback", e);
            mc.execute(TitleScreenMusic::playBundledFallback);
        }
    }

    private static void playBundledFallback() {
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(FALLBACK_SOUND);
        if (sound == null) {
            MeteorClient.LOG.warn("Bundled title music missing ({})", FALLBACK_SOUND);
            return;
        }

        mcSound = new TcoTitleMusicSound(sound);
        mc.getSoundManager().play(mcSound);
        MeteorClient.LOG.info("Playing bundled title music fallback");
    }

    public static void stop() {
        started = false;

        if (wavClip != null) {
            try {
                wavClip.stop();
                wavClip.close();
            } catch (Exception ignored) {}
            wavClip = null;
        }

        if (mc != null && mcSound != null) {
            mc.getSoundManager().stop(mcSound);
            mcSound = null;
            mc.getSoundManager().stop(FALLBACK_SOUND, SoundSource.MUSIC);
        }
    }
}
