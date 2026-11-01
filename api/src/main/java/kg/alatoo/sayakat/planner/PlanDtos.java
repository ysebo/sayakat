package kg.alatoo.sayakat.planner;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kg.alatoo.sayakat.marketplace.ServiceOffering;
import kg.alatoo.sayakat.planner.domain.Pace;
import kg.alatoo.sayakat.planner.persistence.TripPlan;
import kg.alatoo.sayakat.planner.persistence.TripPlanDay;
import kg.alatoo.sayakat.planner.persistence.TripPlanStop;
import kg.alatoo.sayakat.poi.Poi;
import kg.alatoo.sayakat.poi.PoiCategory;

public final class PlanDtos {

    private PlanDtos() {
    }

    public record PlanRequest(
            @NotNull LocalDate startDate,
            @Min(1) @Max(21) int days,
            @Positive int budgetKgs,
            @NotNull Pace pace,
            Set<PoiCategory> interests,
            @NotNull Long startPoiId,
            Long endPoiId,
            TripPlan.Algorithm algorithm) {
    }

    public record StopResponse(int seq, Long poiId, String name, String category, double lat, double lon,
                               String arrival, String departure) {
    }

    public record DayResponse(int dayIndex, LocalDate date, List<StopResponse> stops, Long lodgingServiceId,
                              String lodgingName, Double lodgingLat, Double lodgingLon, int travelMin, int costKgs,
                              String endsAt) {
    }

    public record PlaceRef(Long id, String name, double lat, double lon) {
        static PlaceRef of(Poi p) {
            return new PlaceRef(p.getId(), p.getName(), p.lat(), p.lon());
        }
    }

    public record PlanResponse(Long id, TripPlan.Status status, TripPlan.Algorithm algorithm, LocalDate startDate,
                               int days, int budgetKgs, Pace pace, List<String> interests, PlaceRef start,
                               PlaceRef end, String score, Boolean feasible, Integer totalCostKgs,
                               Integer totalTravelMin, Integer totalValue, String error, Instant createdAt,
                               Instant solvedAt, List<DayResponse> itinerary) {

        public static PlanResponse of(TripPlan p) {
            return new PlanResponse(p.getId(), p.getStatus(), p.getAlgorithm(), p.getStartDate(), p.getDays(),
                    p.getBudgetKgs(), p.getPace(), p.getInterests(), PlaceRef.of(p.getStartPoi()),
                    PlaceRef.of(p.getEndPoi()), p.getScore(), p.getFeasible(), p.getTotalCostKgs(),
                    p.getTotalTravelMin(), p.getTotalValue(), p.getError(), p.getCreatedAt(), p.getSolvedAt(),
                    p.getItinerary().stream().map(PlanResponse::day).toList());
        }

        private static DayResponse day(TripPlanDay d) {
            ServiceOffering lodging = d.getLodgingService();
            boolean located = lodging != null && lodging.getLocation() != null;
            return new DayResponse(d.getDayIndex(), d.getDate(), d.getStops().stream().map(PlanResponse::stop).toList(),
                    lodging == null ? null : lodging.getId(), d.getLodgingName(),
                    located ? lodging.getLocation().getY() : null, located ? lodging.getLocation().getX() : null,
                    d.getTravelMin(), d.getCostKgs(), clock(d.getEndMin()));
        }

        private static StopResponse stop(TripPlanStop s) {
            return new StopResponse(s.getSeq(), s.getPoi().getId(), s.getPoi().getName(), s.getPoi().getCategory().name(),
                    s.getPoi().lat(), s.getPoi().lon(), clock(s.getArrivalMin()), clock(s.getDepartureMin()));
        }
    }

    static String clock(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }
}
