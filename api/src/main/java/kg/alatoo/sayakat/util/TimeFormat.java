package kg.alatoo.sayakat.util;

public final class TimeFormat {

    private TimeFormat() {
    }

    public static String clock(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }
}
