/*
 * tco client — Windows XP style boot splash
 */

package meteordevelopment.meteorclient.tco;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class XpBootScreen extends Screen {
    private int ticks;
    private int progress;

    public XpBootScreen() {
        super(Component.literal("Starting Windows"));
    }

    @Override
    public void tick() {
        ticks++;

        if (ticks % 2 == 0 && progress < 100) progress++;

        if (progress >= 100 && ticks > 140) {
            TcoBoot.markBootComplete();
            mc.setScreen(new TitleScreen());
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int w = width;
        int h = height;

        graphics.fill(0, 0, w, h, 0xFF245EDC);

        int barW = Math.min(360, w - 80);
        int barH = 18;
        int barX = (w - barW) / 2;
        int barY = h / 2 + 24;

        graphics.fill(barX - 2, barY - 2, barX + barW + 2, barY + barH + 2, 0xFF6B6B6B);
        graphics.fill(barX, barY, barX + barW, barY + barH, 0xFFECE9D8);

        int fillW = (int) (barW * (progress / 100f));
        if (fillW > 0) {
            graphics.fill(barX, barY, barX + fillW, barY + barH, 0xFF1E4FA8);
            graphics.fill(barX, barY, barX + fillW, barY + 4, 0xFF5C9CFF);
        }

        String title = "Microsoft\u00AE Windows\u00AE XP";
        int titleW = mc.font.width(title);
        graphics.text(mc.font, Component.literal(title), (w - titleW) / 2, barY - 28, 0xFFFFFFFF);

        String status = bootStatus();
        int statusW = mc.font.width(status);
        graphics.text(mc.font, Component.literal(status), (w - statusW) / 2, barY + barH + 12, 0xFFFFFFFF);

        String footer = "tco client";
        int footerW = mc.font.width(footer);
        graphics.text(mc.font, Component.literal(footer), (w - footerW) / 2, h - 32, 0xFFE8E8E8);
    }

    private String bootStatus() {
        if (progress < 20) return "Starting Windows...";
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
