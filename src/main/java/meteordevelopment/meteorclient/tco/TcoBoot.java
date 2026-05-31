/*
 * tco client — boot splash state
 */

package meteordevelopment.meteorclient.tco;

public final class TcoBoot {
    private static boolean bootComplete;

    private TcoBoot() {}

    public static boolean isBootComplete() {
        return bootComplete;
    }

    public static void markBootComplete() {
        bootComplete = true;
    }
}
