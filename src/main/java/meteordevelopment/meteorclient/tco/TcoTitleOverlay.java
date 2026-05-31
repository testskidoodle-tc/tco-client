/*
 * tco client — animated title splashes + live video background
 */

package meteordevelopment.meteorclient.tco;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fStack;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public final class TcoTitleOverlay {
    private static int ticks;
    private static int splashIndex;

    private TcoTitleOverlay() {}

    public static void tick() {
        ticks++;
        TcoTitleVideoPlayer.tick();

        if (ticks % 140 == 0) {
            splashIndex = (splashIndex + 1) % TcoSplashes.ENTRIES.size();
        }
    }

    public static void reset() {
        ticks = 0;
        splashIndex = 0;
        TcoMediaCache.ensureAsync(TcoTitleVideoPlayer::start);
    }

    public static void renderBackground(GuiGraphicsExtractor graphics, int width, int height) {
        TcoTitleVideoPlayer.render(graphics, width, height);
    }

    public static void renderSplash(GuiGraphicsExtractor graphics, int width, int height) {
        TcoSplashes.Entry entry = TcoSplashes.ENTRIES.get(splashIndex % TcoSplashes.ENTRIES.size());

        float wave = (float) Math.sin(ticks * 0.12);
        float pulse = 1.0f + 0.08f * (float) Math.sin(ticks * 0.2);
        int color = animateColor(entry.baseColor(), ticks);

        int x = (int) (width / 2f + 90 + wave * 6);
        int y = 70;

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.rotate((float) Math.toRadians(-18 + wave * 3));
        pose.scale(pulse, pulse);

        Component text = Component.literal(entry.text());
        int textW = mc.font.width(text);
        graphics.text(mc.font, text, -textW / 2, 0, color);
        pose.popMatrix();
    }

    private static int animateColor(int base, int tick) {
        float hueShift = (tick % 360) / 360f;
        int r = (base >> 16) & 0xFF;
        int g = (base >> 8) & 0xFF;
        int b = base & 0xFF;

        float[] hsv = rgbToHsv(r, g, b);
        hsv[0] = (hsv[0] + hueShift * 0.15f) % 1f;
        hsv[1] = Mth.clamp(hsv[1] + 0.15f * (float) Math.sin(tick * 0.1), 0.4f, 1f);
        hsv[2] = Mth.clamp(hsv[2] + 0.1f * (float) Math.sin(tick * 0.17 + 1), 0.65f, 1f);

        int[] rgb = hsvToRgb(hsv[0], hsv[1], hsv[2]);
        return 0xFF000000 | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
    }

    private static float[] rgbToHsv(int r, int g, int b) {
        float rf = r / 255f, gf = g / 255f, bf = b / 255f;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;

        float h = 0;
        if (delta != 0) {
            if (max == rf) h = ((gf - bf) / delta) % 6;
            else if (max == gf) h = (bf - rf) / delta + 2;
            else h = (rf - gf) / delta + 4;
            h /= 6f;
            if (h < 0) h += 1f;
        }

        float s = max == 0 ? 0 : delta / max;
        return new float[]{h, s, max};
    }

    private static int[] hsvToRgb(float h, float s, float v) {
        float c = v * s;
        float x = c * (1 - Math.abs((h * 6) % 2 - 1));
        float m = v - c;

        float rp = 0, gp = 0, bp = 0;
        int sector = (int) (h * 6) % 6;
        switch (sector) {
            case 0 -> { rp = c; gp = x; }
            case 1 -> { rp = x; gp = c; }
            case 2 -> { gp = c; bp = x; }
            case 3 -> { gp = x; bp = c; }
            case 4 -> { rp = x; bp = c; }
            case 5 -> { rp = c; bp = x; }
        }

        return new int[]{
            (int) ((rp + m) * 255),
            (int) ((gp + m) * 255),
            (int) ((bp + m) * 255)
        };
    }
}
