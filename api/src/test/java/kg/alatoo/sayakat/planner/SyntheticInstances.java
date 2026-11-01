package kg.alatoo.sayakat.planner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import kg.alatoo.sayakat.planner.domain.Lodging;
import kg.alatoo.sayakat.planner.domain.Pace;
import kg.alatoo.sayakat.planner.domain.TripSolution;
import kg.alatoo.sayakat.planner.routing.GeoPoint;
import kg.alatoo.sayakat.planner.routing.HaversineMatrixBuilder;
import kg.alatoo.sayakat.planner.routing.TravelMatrix;
import kg.alatoo.sayakat.planner.solver.TripProblemFactory.CandidateLodging;
import kg.alatoo.sayakat.planner.solver.TripProblemFactory.CandidatePoi;
import kg.alatoo.sayakat.planner.solver.TripProblemFactory.TripRequest;
import kg.alatoo.sayakat.planner.solver.TripProblemFactory;

public final class SyntheticInstances {

    public static final GeoPoint BISHKEK = new GeoPoint(42.8746, 74.5698);
    static final String[] CATEGORIES = {"NATURE", "LAKE", "HIKING", "HISTORY", "CULTURE", "MUSEUM", "BAZAAR", "HOT_SPRING"};
    static final String[] TAGS = {"photo", "family", "horse", "yurt", "mountains", "silk-road", "food", "walking"};

    private SyntheticInstances() {
    }

    public static TripSolution generate(int poiCount, int dayCount, Pace pace, int budgetKgs,
                                        Set<String> interests, LocalDate startDate, long seed) {
        Random random = new Random(seed);
        List<GeoPoint> points = new ArrayList<>();
        List<CandidatePoi> pois = new ArrayList<>();
        GeoPoint[] centres = {BISHKEK, new GeoPoint(42.45, 77.5), new GeoPoint(41.43, 75.99),
                new GeoPoint(40.53, 72.80), new GeoPoint(42.52, 72.24)};

        for (int i = 0; i < poiCount; i++) {
            GeoPoint centre = centres[random.nextInt(centres.length)];
            GeoPoint p = new GeoPoint(centre.lat() + random.nextGaussian() * 0.35,
                    centre.lon() + random.nextGaussian() * 0.6);
            points.add(p);
            String category = CATEGORIES[random.nextInt(CATEGORIES.length)];
            List<String> tags = List.of(TAGS[random.nextInt(TAGS.length)], TAGS[random.nextInt(TAGS.length)]);
            boolean hasHours = category.equals("MUSEUM") || category.equals("BAZAAR") || random.nextDouble() < 0.15;
            int opens = hasHours ? 9 * 60 : 0;
            int closes = hasHours ? (category.equals("BAZAAR") ? 16 * 60 : 18 * 60) : TripProblemFactory.MINUTES_PER_DAY;
            double s = random.nextDouble();
            int seasonFrom = s < 0.3 ? 6 : s < 0.45 ? 5 : 1;
            int seasonTo = s < 0.3 ? 9 : s < 0.45 ? 10 : 12;
            pois.add(new CandidatePoi(i + 1, "POI-" + (i + 1), category, tags, i,
                    30 + 15 * random.nextInt(14), opens, closes, random.nextDouble() < 0.5 ? 0 : 100 * random.nextInt(8),
                    seasonFrom, seasonTo, 1 + random.nextInt(5)));
        }

        int lodgingCount = Math.max(3, poiCount / 4);
        List<CandidateLodging> lodgings = new ArrayList<>();
        for (int i = 0; i < lodgingCount; i++) {
            GeoPoint near = points.get(random.nextInt(points.size()));
            points.add(new GeoPoint(near.lat() + random.nextGaussian() * 0.03, near.lon() + random.nextGaussian() * 0.03));
            lodgings.add(new CandidateLodging((long) (1000 + i), "Guesthouse-" + (i + 1), points.size() - 1,
                    800 + 200 * random.nextInt(10)));
        }

        points.add(BISHKEK);
        int bishkekIndex = points.size() - 1;
        TravelMatrix matrix = HaversineMatrixBuilder.build(points, 1.4, 50);

        TripRequest request = new TripRequest(startDate, dayCount, pace, budgetKgs, interests);
        return TripProblemFactory.build(request, pois, lodgings, Lodging.of(null, "Bishkek hotel", bishkekIndex, 2500),
                bishkekIndex, 12, 45, matrix);
    }
}
