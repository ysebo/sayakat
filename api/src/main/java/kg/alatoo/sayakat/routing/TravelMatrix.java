package kg.alatoo.sayakat.routing;

public final class TravelMatrix {

    private final int[][] minutes;
    private final int[][] meters;

    public TravelMatrix(int[][] minutes, int[][] meters) {
        if (minutes.length != meters.length) {
            throw new IllegalArgumentException("minutes and meters must have the same size");
        }
        this.minutes = minutes;
        this.meters = meters;
    }

    public int minutes(int from, int to) {
        return minutes[from][to];
    }

    public int meters(int from, int to) {
        return meters[from][to];
    }

    public int size() {
        return minutes.length;
    }
}
