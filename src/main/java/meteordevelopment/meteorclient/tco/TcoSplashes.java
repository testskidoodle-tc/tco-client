/*
 * tco client — custom title splashes
 */

package meteordevelopment.meteorclient.tco;

import java.util.List;

public final class TcoSplashes {
    public record Entry(String text, int baseColor) {}

    public static final List<Entry> ENTRIES = List.of(
        new Entry("noah jenkins", 0xFF55FFFF),
        new Entry("ralph ortiz", 0xFF55FF55),
        new Entry("DJP CUM", 0xFFFF5555),
        new Entry("big scripts", 0xFFAA55FF),
        new Entry("idk how to update this shit properly im ngl", 0xFFAAAAAA),
        new Entry("wow!", 0xFFFFD700),
        new Entry("WELCOME TO HELL.", 0xFFFF3300)
    );

    private TcoSplashes() {}
}
