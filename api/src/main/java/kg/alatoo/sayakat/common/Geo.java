package kg.alatoo.sayakat.common;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import kg.alatoo.sayakat.routing.GeoPoint;

public final class Geo {

    public static final int WGS84 = 4326;
    private static final GeometryFactory FACTORY = new GeometryFactory(new PrecisionModel(), WGS84);

    private Geo() {
    }

    public static Point point(double lat, double lon) {
        return FACTORY.createPoint(new Coordinate(lon, lat));
    }

    public static GeoPoint toGeoPoint(Point point) {
        return new GeoPoint(point.getY(), point.getX());
    }
}
