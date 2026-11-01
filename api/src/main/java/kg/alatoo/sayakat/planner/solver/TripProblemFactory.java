package kg.alatoo.sayakat.planner.solver;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import kg.alatoo.sayakat.planner.domain.Lodging;
import kg.alatoo.sayakat.planner.domain.Pace;
import kg.alatoo.sayakat.planner.domain.TripDay;
import kg.alatoo.sayakat.planner.domain.TripSettings;
import kg.alatoo.sayakat.planner.domain.TripSolution;
import kg.alatoo.sayakat.planner.domain.Visit;
import kg.alatoo.sayakat.routing.TravelMatrix;

public final class TripProblemFactory {

    public static final int MINUTES_PER_DAY = 24 * 60;

    public record TripRequest(LocalDate startDate, int dayCount, Pace pace, int budgetKgs, Set<String> interests) {
    }

    public record CandidatePoi(long id, String name, String category, List<String> tags, int locationIndex,
                               int durationMin, int opensMin, int closesMin, int costKgs,
                               int seasonFrom, int seasonTo, int popularity) {
    }

    public record CandidateLodging(Long serviceId, String name, int locationIndex, int priceKgs) {
    }

    public static TripSolution build(TripRequest request, List<CandidatePoi> pois, List<CandidateLodging> lodgings,
                                     Lodging startLodging, int endLocationIndex, int transportCostPerKm,
                                     int maxLodgingTransferMin, TravelMatrix matrix) {
        TripSettings settings = new TripSettings(request.startDate(), request.dayCount(), request.pace(),
                request.budgetKgs(), request.interests(), startLodging, endLocationIndex, transportCostPerKm, matrix);

        List<TripDay> days = new ArrayList<>(request.dayCount());
        for (int i = 0; i < request.dayCount(); i++) {
            days.add(new TripDay(i, request.startDate().plusDays(i), settings));
        }

        List<Lodging> allLodgings = new ArrayList<>(lodgings.size() + 1);
        allLodgings.add(startLodging);
        for (CandidateLodging l : lodgings) {
            allLodgings.add(Lodging.of(l.serviceId(), l.name(), l.locationIndex(), l.priceKgs()));
        }

        List<Visit> visits = new ArrayList<>(pois.size());
        for (CandidatePoi poi : pois) {
            Visit probe = new Visit(poi.id(), poi.name(), poi.locationIndex(), poi.durationMin(), poi.opensMin(),
                    poi.closesMin(), poi.costKgs(), poi.seasonFrom(), poi.seasonTo(), 0, null);
            if (days.stream().noneMatch(d -> probe.isInSeason(d.getDate()))) {
                continue;
            }
            int value = ValueModel.value(poi.category(), poi.tags(), poi.popularity(), request.interests());
            Lodging nearest = nearestLodging(poi.locationIndex(), allLodgings, maxLodgingTransferMin, matrix);
            visits.add(new Visit(poi.id(), poi.name(), poi.locationIndex(), poi.durationMin(), poi.opensMin(),
                    poi.closesMin(), poi.costKgs(), poi.seasonFrom(), poi.seasonTo(), value, nearest));
        }
        return new TripSolution(settings, days, visits);
    }

    private static Lodging nearestLodging(int from, List<Lodging> lodgings, int maxTransferMin, TravelMatrix matrix) {
        Lodging best = null;
        int bestMin = Integer.MAX_VALUE;
        for (Lodging lodging : lodgings) {
            int min = matrix.minutes(from, lodging.locationIndex());
            if (min <= maxTransferMin && min < bestMin) {
                best = lodging;
                bestMin = min;
            }
        }
        return best;
    }
}
