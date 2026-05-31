/*
 * tco client — download YouTube title media and extract audio for the title screen
 */

package meteordevelopment.meteorclient.tco;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TcoMediaCache {
    public static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=iu_0kOfMGD0";

    public static final Path TITLE_DIR = MeteorClient.FOLDER.toPath().resolve("title");
    public static final Path VIDEO = TITLE_DIR.resolve("video.mp4");
    public static final Path AUDIO_WAV = TITLE_DIR.resolve("title_audio.wav");

    private static final AtomicBoolean PREPARING = new AtomicBoolean(false);

    private TcoMediaCache() {}

    public static boolean isReady() {
        return fileSize(VIDEO) > 1000 && fileSize(AUDIO_WAV) > 1000;
    }

    private static long fileSize(Path path) {
        try {
            return Files.isRegularFile(path) ? Files.size(path) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public static void ensureAsync(Runnable onReady) {
        if (isReady()) {
            onReady.run();
            return;
        }

        if (!PREPARING.compareAndSet(false, true)) return;

        MeteorExecutor.execute(() -> {
            try {
                prepareBlocking();
                onReady.run();
            } catch (Exception e) {
                MeteorClient.LOG.error("Failed to prepare title media", e);
            } finally {
                PREPARING.set(false);
            }
        });
    }

    public static void prepareBlocking() throws Exception {
        Files.createDirectories(TITLE_DIR);

        if (!Files.isRegularFile(VIDEO) || Files.size(VIDEO) == 0) {
            downloadVideo();
        }

        if (!Files.isRegularFile(AUDIO_WAV) || Files.size(AUDIO_WAV) == 0) {
            extractAudio();
        }
    }

    private static void downloadVideo() throws Exception {
        MeteorClient.LOG.info("Downloading title video from YouTube...");

        List<String> cmd = new ArrayList<>();
        cmd.add(findPython());
        cmd.add("-m");
        cmd.add("yt_dlp");
        cmd.add("-f");
        cmd.add("bestvideo[height<=1080]+bestaudio/best[height<=1080]");
        cmd.add("--merge-output-format");
        cmd.add("mp4");
        cmd.add("-o");
        cmd.add(VIDEO.toString());
        cmd.add(YOUTUBE_URL);

        run(cmd, 10, TimeUnit.MINUTES);
    }

    private static void extractAudio() throws Exception {
        String ffmpeg = TcoFfmpeg.getPath();
        if (ffmpeg == null) throw new IllegalStateException("ffmpeg not found");

        MeteorClient.LOG.info("Extracting title audio from video...");

        List<String> cmd = List.of(
            ffmpeg,
            "-y",
            "-i", VIDEO.toString(),
            "-vn",
            "-acodec", "pcm_s16le",
            "-ar", "48000",
            "-ac", "2",
            AUDIO_WAV.toString()
        );

        run(cmd, 3, TimeUnit.MINUTES);
    }

    private static String findPython() {
        for (String candidate : List.of("python", "python3", "py")) {
            try {
                Process p = new ProcessBuilder(candidate, "--version")
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();
                if (p.waitFor(5, TimeUnit.SECONDS) && p.exitValue() == 0) return candidate;
            } catch (Exception ignored) {}
        }
        return "python";
    }

    private static void run(List<String> command, long timeout, TimeUnit unit) throws Exception {
        Process process = new ProcessBuilder(command)
            .directory(TITLE_DIR.toFile())
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start();

        if (!process.waitFor(timeout, unit)) {
            process.destroyForcibly();
            throw new IllegalStateException("Command timed out: " + String.join(" ", command));
        }

        if (process.exitValue() != 0) {
            throw new IllegalStateException("Command failed (" + process.exitValue() + "): " + String.join(" ", command));
        }
    }
}
