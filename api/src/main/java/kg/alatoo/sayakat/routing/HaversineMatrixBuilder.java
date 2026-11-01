package kg.alatoo.sayakat.routing;

import java.util.List;

public final class HaversineMatrixBuilder {

    private HaversineMatrixBuilder() {
    }

    public static TravelMatrix build(List<GeoPoint> points, double detourFactor, double speedKmh) {
        int n = points.size();
        int[][] minutes = new int[n][n];
        int[][] meters = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }
                double roadMeters = points.get(i).distanceMeters(points.get(j)) * detourFactor;
                meters[i][j] = (int) Math.round(roadMeters);
                minutes[i][j] = (int) Math.ceil(roadMeters / 1000.0 / speedKmh * 60.0);
            }
        }
        return new TravelMatrix(minutes, meters);
    }
}
