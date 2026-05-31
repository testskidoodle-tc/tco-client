/*
 * tco client — locate ffmpeg executable
 */

package meteordevelopment.meteorclient.tco;

import meteordevelopment.meteorclient.MeteorClient;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class TcoFfmpeg {
    private static String cached;

    private TcoFfmpeg() {}

    public static String getPath() {
        if (cached != null) return cached;

        List<String> candidates = new ArrayList<>();
        candidates.add("ffmpeg");

        String local = System.getenv("LOCALAPPDATA");
        if (local != null) {
            candidates.add(local + "\\Microsoft\\WinGet\\Links\\ffmpeg.exe");
        }

        Path modBin = MeteorClient.FOLDER.toPath().resolve("bin/ffmpeg.exe");
        candidates.add(modBin.toString());

        for (String candidate : candidates) {
            if (canRun(candidate)) {
                cached = candidate;
                MeteorClient.LOG.info("Using ffmpeg at {}", cached);
                return cached;
            }
        }

        return null;
    }

    private static boolean canRun(String command) {
        try {
            Process process = new ProcessBuilder(command, "-version")
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .start();

            return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isAvailable() {
        return getPath() != null;
    }
}
