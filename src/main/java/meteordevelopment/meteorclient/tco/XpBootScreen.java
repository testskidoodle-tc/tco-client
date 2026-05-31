/*
 * tco client — Windows XP style boot splash ("tco OS xp")
 */

package meteordevelopment.meteorclient.tco;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class XpBootScreen extends Screen {
    private static final Identifier BOOT_TEXTURE = Identifier.fromNamespaceAndPath(
        MeteorClient.MOD_ID, "textures/tco/xp_boot_reference.png"
    );

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

        // Full-screen reference art (flag + layout), then we paint text on top
        graphics.blit(RenderPipelines.GUI_TEXTURED, BOOT_TEXTURE, 0, 0, 0, 0, w, h, w, h, w, h, ARGB.white(1f));

        int centerY = h / 2 - 10;

        // Cover vanilla "Microsoft / Windows" area with black so we can draw tco branding
        graphics.fill(cx - 200, centerY - 8, cx + 200, centerY + 36, 0xFF000000);

        // Small label
        String small = "tco\u00AE";
        int smallW = mc.font.width(small);
        graphics.text(mc.font, Component.literal(small), cx - smallW / 2, centerY - 14, 0xFFFFFFFF);

        // "tco OS" + orange "xp"
        String os = "tco OS";
        String xp = "xp";
        int osW = mc.font.width(os);
        int xpW = mc.font.width(xp);
        int totalW = osW + xpW + 4;
        int textX = cx - totalW / 2;

        graphics.text(mc.font, Component.literal(os).withStyle(Style.EMPTY.withBold(true)), textX, centerY + 2, 0xFFFFFFFF);
        graphics.text(
            mc.font,
            Component.literal(xp).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withItalic(true)),
            textX + osW + 4,
            centerY + 8,
            0xFFFF8C00
        );

        // XP segmented loading bar (3 blocks)
        int barW = 180;
        int barH = 14;
        int barX = cx - barW / 2;
        int barY = centerY + 42;

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

        // Footer (classic XP layout)
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

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
