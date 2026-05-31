/*
 * tco client — mouse-reactive grid / network overlay on title screen
 */

package meteordevelopment.meteorclient.tco;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public final class TcoTitleGridNetwork {
    private static final int GRID_SPACING = 56;
    private static final int MOUSE_RADIUS = 220;
    private static final int LINE_COLOR = 0x55A8D8FF;
    private static final int NODE_COLOR = 0xCC4FC3F7;
    private static final int GRID_COLOR = 0x18FFFFFF;

    private static float targetX = -1;
    private static float targetY = -1;
    private static float smoothX;
    private static float smoothY;
    private static int ticks;

    private TcoTitleGridNetwork() {}

    public static void setMouse(int mouseX, int mouseY) {
        targetX = mouseX;
        targetY = mouseY;
    }

    public static void tick(int width, int height) {
        ticks++;

        if (targetX < 0) {
            smoothX = width / 2f;
            smoothY = height / 2f;
            targetX = smoothX;
            targetY = smoothY;
            return;
        }

        smoothX += (targetX - smoothX) * 0.22f;
        smoothY += (targetY - smoothY) * 0.22f;
    }

    public static void render(GuiGraphicsExtractor graphics, int width, int height) {
        drawGrid(graphics, width, height);

        int mx = (int) smoothX;
        int my = (int) smoothY;

        List<int[]> nodes = collectNodes(mx, my, width, height);
        nodes.add(new int[]{mx, my});

        float pulse = 0.65f + 0.35f * (float) Math.sin(ticks * 0.14);

        for (int[] node : nodes) {
            int dist = distSq(node[0], node[1], mx, my);
            int alpha = (int) (120 * pulse * (1f - Math.min(1f, dist / (float) (MOUSE_RADIUS * MOUSE_RADIUS))));
            int lineCol = (LINE_COLOR & 0x00FFFFFF) | (Mth.clamp(alpha, 24, 200) << 24);

            drawLine(graphics, node[0], node[1], mx, my, lineCol, 2);
        }

        for (int i = 0; i < nodes.size(); i++) {
            int[] a = nodes.get(i);
            for (int j = i + 1; j < nodes.size(); j++) {
                int[] b = nodes.get(j);
                int d = distSq(a[0], a[1], b[0], b[1]);
                if (d > GRID_SPACING * GRID_SPACING * 2.2f) continue;

                int alpha = (int) (80 * pulse);
                int lineCol = (LINE_COLOR & 0x00FFFFFF) | (Mth.clamp(alpha, 16, 120) << 24);
                drawLine(graphics, a[0], a[1], b[0], b[1], lineCol, 1);
            }
        }

        for (int[] node : nodes) {
            int size = node[0] == mx && node[1] == my ? 5 : 3;
            int half = size / 2;
            graphics.fill(node[0] - half, node[1] - half, node[0] + half + 1, node[1] + half + 1, NODE_COLOR);
        }

        graphics.fill(mx - 6, my - 6, mx + 7, my + 7, 0x44FFFFFF);
        graphics.fill(mx - 3, my - 3, mx + 4, my + 4, 0xEEFFFFFF);
    }

    private static void drawGrid(GuiGraphicsExtractor graphics, int width, int height) {
        for (int x = 0; x < width; x += GRID_SPACING) {
            graphics.fill(x, 0, x + 1, height, GRID_COLOR);
        }
        for (int y = 0; y < height; y += GRID_SPACING) {
            graphics.fill(0, y, width, y + 1, GRID_COLOR);
        }
    }

    private static List<int[]> collectNodes(int mx, int my, int width, int height) {
        List<int[]> nodes = new ArrayList<>();

        int minGx = ((mx - MOUSE_RADIUS) / GRID_SPACING) * GRID_SPACING;
        int maxGx = ((mx + MOUSE_RADIUS) / GRID_SPACING + 1) * GRID_SPACING;
        int minGy = ((my - MOUSE_RADIUS) / GRID_SPACING) * GRID_SPACING;
        int maxGy = ((my + MOUSE_RADIUS) / GRID_SPACING + 1) * GRID_SPACING;

        for (int gx = minGx; gx <= maxGx; gx += GRID_SPACING) {
            for (int gy = minGy; gy <= maxGy; gy += GRID_SPACING) {
                if (gx < 0 || gy < 0 || gx > width || gy > height) continue;
                int d = distSq(gx, gy, mx, my);
                if (d <= MOUSE_RADIUS * MOUSE_RADIUS) {
                    float wobble = (float) Math.sin(ticks * 0.1 + gx * 0.05 + gy * 0.05) * 3;
                    nodes.add(new int[]{(int) (gx + wobble), (int) (gy + wobble * 0.7)});
                }
            }
        }

        return nodes;
    }

    private static void drawLine(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color, int thickness) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        if (steps == 0) return;

        for (int i = 0; i <= steps; i++) {
            float t = i / (float) steps;
            int x = (int) (x1 + (x2 - x1) * t);
            int y = (int) (y1 + (y2 - y1) * t);
            graphics.fill(x, y, x + thickness, y + thickness, color);
        }
    }

    private static int distSq(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        return dx * dx + dy * dy;
    }

    public static void reset() {
        targetX = -1;
        targetY = -1;
        ticks = 0;
    }
}
