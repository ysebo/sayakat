package kg.alatoo.sayakat.planner.domain;

import java.time.LocalDate;
import java.util.Set;

import kg.alatoo.sayakat.planner.routing.TravelMatrix;

public record TripSettings(
        LocalDate startDate,
        int dayCount,
        Pace pace,
        int budgetKgs,
        Set<String> interests,
        Lodging startLodging,
        int endLocationIndex,
        int transportCostPerKm,
        TravelMatrix matrix) {
}
