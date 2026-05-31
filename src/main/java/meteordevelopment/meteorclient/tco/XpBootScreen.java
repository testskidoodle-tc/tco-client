/*
 * tco client — Windows XP style boot splash ("tco OS xp") — procedural, no overlap
 */

package meteordevelopment.meteorclient.tco;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class XpBootScreen extends Screen {
    private int ticks;
    private int progress;

    public XpBootScreen() {
        super(Component.literal("Starting tco OS"));
    }

    @Override
    public void tick() {
        ticks++;

        if (ticks % 2 == 0 && progress < 100) progress++;

        if (progress >= 100 && ticks > 160) {
            TcoBoot.markBootComplete();
            mc.setScreen(new TitleScreen());
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xFF000000);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int w = width;
        int h = height;
        int cx = w / 2;
        int centerY = h / 2 - 20;

        drawWindowsFlag(graphics, cx, centerY - 52);

        String small = "tco\u00AE";
        int smallW = mc.font.width(small);
        graphics.text(mc.font, Component.literal(small), cx - smallW / 2, centerY - 22, 0xFFFFFFFF);

        String os = "tco OS";
        String xp = "xp";
        int osW = mc.font.width(os);
        int xpW = mc.font.width(xp);
        int textX = cx - (osW + xpW + 6) / 2;

        graphics.text(mc.font, Component.literal(os).withStyle(Style.EMPTY.withBold(true)), textX, centerY, 0xFFFFFFFF);
        graphics.text(
            mc.font,
            Component.literal(xp).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withItalic(true)),
            textX + osW + 6,
            centerY + 6,
            0xFFFF8C00
        );

        int barW = 180;
        int barH = 14;
        int barX = cx - barW / 2;
        int barY = centerY + 38;

        graphics.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF3A3A3A);
        graphics.fill(barX, barY, barX + barW, barY + barH, 0xFF1A1A1A);

        int segmentW = (barW - 8) / 3;
        int litSegments = Math.min(3, progress / 34 + (ticks / 12) % 2);
        for (int i = 0; i < 3; i++) {
            int sx = barX + 4 + i * (segmentW + 2);
            int color = i < litSegments ? 0xFF2B6FD6 : 0xFF0D0D0D;
            graphics.fill(sx, barY + 3, sx + segmentW, barY + barH - 3, color);
            if (i < litSegments) {
                graphics.fill(sx, barY + 3, sx + segmentW, barY + 5, 0xFF6BA3FF);
            }
        }

        String status = bootStatus();
        int statusW = mc.font.width(status);
        graphics.text(mc.font, Component.literal(status), cx - statusW / 2, barY + barH + 10, 0xFFCCCCCC);

        graphics.text(mc.font, Component.literal("Copyright \u00A9 tco Corporation"), 8, h - 28, 0xFFFFFFFF);
        String brand = "tco";
        int brandW = mc.font.width(brand);
        graphics.text(
            mc.font,
            Component.literal(brand).withStyle(Style.EMPTY.withBold(true).withItalic(true)),
            w - brandW - 12,
            h - 32,
            0xFFFFFFFF
        );
    }

    private static void drawWindowsFlag(GuiGraphicsExtractor graphics, int cx, int cy) {
        graphics.fill(cx - 50, cy - 6, cx - 14, cy + 28, 0xFFF07800);
        graphics.fill(cx - 10, cy - 30, cx + 26, cy - 2, 0xFF00A000);
        graphics.fill(cx - 46, cy + 2, cx - 10, cy + 30, 0xFF0078D7);
        graphics.fill(cx - 6, cy + 2, cx + 30, cy + 30, 0xFFFFCC00);
    }

    private String bootStatus() {
        if (progress < 20) return "Starting tco OS...";
        if (progress < 45) return "Loading personal settings...";
        if (progress < 70) return "Loading network connections...";
        if (progress < 95) return "Preparing tco client...";
        return "Welcome";
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
