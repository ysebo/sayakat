package kg.alatoo.sayakat.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import kg.alatoo.sayakat.entity.TripPlan;
import kg.alatoo.sayakat.entity.enums.PlanAlgorithm;
import kg.alatoo.sayakat.entity.enums.PlanStatus;
import kg.alatoo.sayakat.planner.domain.Pace;

public record PlanResponse(Long id, PlanStatus status, PlanAlgorithm algorithm, LocalDate startDate, int days,
                           int budgetKgs, Pace pace, List<String> interests, PlaceResponse start, PlaceResponse end,
                           String score, Boolean feasible, Integer totalCostKgs, Integer totalTravelMin,
                           Integer totalValue, String error, Instant createdAt, Instant solvedAt,
                           List<DayResponse> itinerary) {

    public static PlanResponse of(TripPlan p) {
        return new PlanResponse(p.getId(), p.getStatus(), p.getAlgorithm(), p.getStartDate(), p.getDays(),
                p.getBudgetKgs(), p.getPace(), p.getInterests(), PlaceResponse.of(p.getStartPoi()),
                PlaceResponse.of(p.getEndPoi()), p.getScore(), p.getFeasible(), p.getTotalCostKgs(),
                p.getTotalTravelMin(), p.getTotalValue(), p.getError(), p.getCreatedAt(), p.getSolvedAt(),
                p.getItinerary().stream().map(DayResponse::of).toList());
    }
}
