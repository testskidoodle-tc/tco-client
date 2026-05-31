/*
 * tco client — 60fps title video: blurred full BG + sharp cropped center (ffmpeg vstack)
 */

package meteordevelopment.meteorclient.tco;

import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.Texture;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public final class TcoTitleVideoPlayer {
    public static final int TEX_WIDTH = 1920;
    public static final int BLUR_HEIGHT = 1080;
    public static final int SHARP_HEIGHT = 540;
    public static final int TEX_HEIGHT = BLUR_HEIGHT + SHARP_HEIGHT;

    private static final int FRAME_BYTES = TEX_WIDTH * TEX_HEIGHT * 3;
    private static final int BLUR_BYTES = TEX_WIDTH * BLUR_HEIGHT * 3;
    private static final int SHARP_BYTES = TEX_WIDTH * SHARP_HEIGHT * 3;

    private static final Identifier BLUR_TEXTURE_ID = Identifier.fromNamespaceAndPath(MeteorClient.MOD_ID, "tco/title_video_blur");
    private static final Identifier SHARP_TEXTURE_ID = Identifier.fromNamespaceAndPath(MeteorClient.MOD_ID, "tco/title_video_sharp");

    private static Texture blurTexture;
    private static Texture sharpTexture;
    private static Process process;
    private static Thread decodeThread;
    private static volatile boolean running;

    private static byte[] latestFrame;
    private static final Object frameLock = new Object();
    private static volatile boolean failed;

    private static byte[] blurRgbaScratch;
    private static byte[] sharpRgbaScratch;

    private TcoTitleVideoPlayer() {}

    /** Crop + lanczos sharp center, blurred widescreen background, 60fps. */
    private static String videoFilter() {
        return ""
            + "[0:v]crop=iw:iw*9/16:(iw-ow)/2:(ih-oh)/2,split=2[base][base2];"
            + "[base]scale=" + TEX_WIDTH + ":" + BLUR_HEIGHT + ":force_original_aspect_ratio=increase,"
            + "crop=" + TEX_WIDTH + ":" + BLUR_HEIGHT + ",boxblur=14:4,fps=60[bg];"
            + "[base2]crop=iw*0.88:ih*0.88:(iw-ow)/2:(ih-oh)/2,"
            + "scale=" + TEX_WIDTH + ":" + SHARP_HEIGHT + ":flags=lanczos,fps=60[fg];"
            + "[bg][fg]vstack=inputs=2[out]";
    }

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

        blurTexture = new Texture(TEX_WIDTH, BLUR_HEIGHT, TextureFormat.RGBA8, FilterMode.LINEAR, FilterMode.LINEAR);
        sharpTexture = new Texture(TEX_WIDTH, SHARP_HEIGHT, TextureFormat.RGBA8, FilterMode.LINEAR, FilterMode.LINEAR);
        mc.getTextureManager().register(BLUR_TEXTURE_ID, blurTexture);
        mc.getTextureManager().register(SHARP_TEXTURE_ID, sharpTexture);

        String ffmpeg = TcoFfmpeg.getPath();
        List<String> cmd = List.of(
            ffmpeg,
            "-nostdin",
            "-threads", "4",
            "-stream_loop", "-1",
            "-i", TcoMediaCache.VIDEO.toString(),
            "-an",
            "-filter_complex", videoFilter(),
            "-map", "[out]",
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

            MeteorClient.LOG.info("Title video: {}x{} blur + {}x{} sharp @ 60fps", TEX_WIDTH, BLUR_HEIGHT, TEX_WIDTH, SHARP_HEIGHT);
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

    public static void tick() {
        if (!running || blurTexture == null || sharpTexture == null) return;

        byte[] frame;
        synchronized (frameLock) {
            frame = latestFrame;
        }
        if (frame == null) return;

        try {
            blurRgbaScratch = rgbToRgba(frame, 0, BLUR_BYTES, blurRgbaScratch);
            sharpRgbaScratch = rgbToRgba(frame, BLUR_BYTES, SHARP_BYTES, sharpRgbaScratch);
            blurTexture.upload(blurRgbaScratch);
            sharpTexture.upload(sharpRgbaScratch);
        } catch (Exception e) {
            MeteorClient.LOG.error("Title video upload error", e);
        }
    }

    public static void render(GuiGraphicsExtractor graphics, int width, int height) {
        if (!running || blurTexture == null || sharpTexture == null) {
            graphics.fill(0, 0, width, height, 0xFF000000);
            return;
        }

        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            BLUR_TEXTURE_ID,
            0, 0,
            0, 0,
            width, height,
            width, height,
            TEX_WIDTH, BLUR_HEIGHT,
            ARGB.white(1f)
        );

        int panelW = (int) (width * 0.82f);
        int panelH = (int) (panelW * (SHARP_HEIGHT / (float) TEX_WIDTH));
        if (panelH > height * 0.55f) {
            panelH = (int) (height * 0.55f);
            panelW = (int) (panelH * (TEX_WIDTH / (float) SHARP_HEIGHT));
        }
        int panelX = (width - panelW) / 2;
        int panelY = (height - panelH) / 2;

        graphics.fill(panelX - 2, panelY - 2, panelX + panelW + 2, panelY + panelH + 2, 0x66000000);
        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            SHARP_TEXTURE_ID,
            panelX, panelY,
            0, 0,
            panelW, panelH,
            panelW, panelH,
            TEX_WIDTH, SHARP_HEIGHT,
            ARGB.white(1f)
        );

        graphics.fill(0, 0, width, height, 0x44000000);
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

        if (blurTexture != null || sharpTexture != null) {
            mc.execute(() -> {
                if (blurTexture != null) {
                    mc.getTextureManager().release(BLUR_TEXTURE_ID);
                    blurTexture = null;
                }
                if (sharpTexture != null) {
                    mc.getTextureManager().release(SHARP_TEXTURE_ID);
                    sharpTexture = null;
                }
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

    private static byte[] rgbToRgba(byte[] rgb, int offset, int byteCount, byte[] scratch) {
        int len = byteCount / 3 * 4;
        byte[] buf = scratch;
        if (buf == null || buf.length != len) buf = new byte[len];

        for (int i = 0, j = 0; i < byteCount; i += 3, j += 4) {
            int idx = offset + i;
            buf[j] = rgb[idx];
            buf[j + 1] = rgb[idx + 1];
            buf[j + 2] = rgb[idx + 2];
            buf[j + 3] = (byte) 255;
        }

        return buf;
    }
}
