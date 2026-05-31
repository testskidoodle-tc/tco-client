/*
 * tco client — 60fps title screen video via ffmpeg pipe → GPU texture
 */

package meteordevelopment.meteorclient.tco;

import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public final class TcoTitleVideoPlayer {
    public static final int VIDEO_WIDTH = 1280;
    public static final int VIDEO_HEIGHT = 720;
    private static final int FRAME_BYTES = VIDEO_WIDTH * VIDEO_HEIGHT * 3;

    private static final Identifier TEXTURE_ID = Identifier.fromNamespaceAndPath(MeteorClient.MOD_ID, "tco/title_video");

    private static Texture texture;
    private static Process process;
    private static Thread decodeThread;
    private static volatile boolean running;

    private static byte[] latestFrame;
    private static final Object frameLock = new Object();
    private static volatile boolean failed;

    private TcoTitleVideoPlayer() {}

    public static void start() {
        if (running || failed) return;
        if (!TcoFfmpeg.isAvailable()) {
            MeteorClient.LOG.warn("ffmpeg not found — title video disabled. Install ffmpeg (winget install Gyan.FFmpeg).");
            failed = true;
            return;
        }
        if (!Files.isRegularFile(TcoMediaCache.VIDEO)) {
            TcoMediaCache.ensureAsync(TcoTitleVideoPlayer::start);
            return;
        }

        stop();

        texture = new Texture(VIDEO_WIDTH, VIDEO_HEIGHT, TextureFormat.RGBA8, FilterMode.LINEAR, FilterMode.LINEAR);
        mc.getTextureManager().register(TEXTURE_ID, texture);

        String ffmpeg = TcoFfmpeg.getPath();
        List<String> cmd = List.of(
            ffmpeg,
            "-nostdin",
            "-threads", "2",
            "-stream_loop", "-1",
            "-i", TcoMediaCache.VIDEO.toString(),
            "-an",
            "-vf", "scale=" + VIDEO_WIDTH + ":" + VIDEO_HEIGHT + ":flags=fast_bilinear,fps=60",
            "-f", "rawvideo",
            "-pix_fmt", "rgb24",
            "pipe:1"
        );

        try {
            process = new ProcessBuilder(cmd)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();

            running = true;
            InputStream pipe = process.getInputStream();

            decodeThread = new Thread(() -> decodeLoop(pipe), "tco-title-video-decode");
            decodeThread.setDaemon(true);
            decodeThread.start();

            MeteorClient.LOG.info("Title video playback started ({}x{} @ 60fps)", VIDEO_WIDTH, VIDEO_HEIGHT);
        } catch (Exception e) {
            MeteorClient.LOG.error("Failed to start title video", e);
            failed = true;
            stop();
        }
    }

    private static void decodeLoop(InputStream pipe) {
        byte[] readBuffer = new byte[FRAME_BYTES];
        synchronized (frameLock) {
            latestFrame = new byte[FRAME_BYTES];
        }

        try {
            while (running) {
                if (!readFully(pipe, readBuffer)) break;
                synchronized (frameLock) {
                    if (latestFrame != null) System.arraycopy(readBuffer, 0, latestFrame, 0, FRAME_BYTES);
                }
            }
        } catch (Exception e) {
            if (running) MeteorClient.LOG.error("Title video decode error", e);
        } finally {
            running = false;
        }
    }

    private static byte[] rgbaScratch;

    /** Upload latest frame (call from title screen tick on the client thread). */
    public static void tick() {
        if (!running || texture == null) return;

        byte[] frame;
        synchronized (frameLock) {
            frame = latestFrame;
        }
        if (frame == null) return;

        try {
            texture.upload(rgbToRgba(frame));
        } catch (Exception e) {
            MeteorClient.LOG.error("Title video upload error", e);
        }
    }

    public static void render(GuiGraphicsExtractor graphics, int width, int height) {
        if (!running || texture == null) {
            graphics.fill(0, 0, width, height, 0xFF000000);
            return;
        }

        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            TEXTURE_ID,
            0, 0,
            0, 0,
            width, height,
            width, height,
            VIDEO_WIDTH, VIDEO_HEIGHT,
            ARGB.white(1f)
        );
        graphics.fill(0, 0, width, height, 0x33000000);
    }

    public static void stop() {
        running = false;

        if (decodeThread != null) {
            decodeThread.interrupt();
            decodeThread = null;
        }

        if (process != null) {
            process.destroyForcibly();
            process = null;
        }

        synchronized (frameLock) {
            latestFrame = null;
        }

        if (texture != null) {
            mc.execute(() -> {
                mc.getTextureManager().release(TEXTURE_ID);
                texture = null;
            });
        }
    }

    public static boolean isPlaying() {
        return running;
    }

    private static boolean readFully(InputStream in, byte[] buffer) throws Exception {
        int offset = 0;
        while (offset < buffer.length) {
            int read = in.read(buffer, offset, buffer.length - offset);
            if (read < 0) return false;
            offset += read;
        }
        return true;
    }

    private static byte[] rgbToRgba(byte[] rgb) {
        int len = rgb.length / 3 * 4;
        if (rgbaScratch == null || rgbaScratch.length != len) rgbaScratch = new byte[len];

        for (int i = 0, j = 0; i < rgb.length; i += 3, j += 4) {
            rgbaScratch[j] = rgb[i];
            rgbaScratch[j + 1] = rgb[i + 1];
            rgbaScratch[j + 2] = rgb[i + 2];
            rgbaScratch[j + 3] = (byte) 255;
        }
        return rgbaScratch;
    }
}
