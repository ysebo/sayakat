package kg.alatoo.sayakat.planner.solver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import kg.alatoo.sayakat.planner.domain.Lodging;
import kg.alatoo.sayakat.planner.domain.TripDay;
import kg.alatoo.sayakat.planner.domain.TripSettings;
import kg.alatoo.sayakat.planner.domain.Visit;
import kg.alatoo.sayakat.routing.TravelMatrix;

public record TripSchedule(
        List<DayResult> days,
        int lateMinutes,
        int overtimeMinutes,
        int nightsWithoutLodging,
        int totalCostKgs,
        int overBudgetKgs,
        int totalTravelMin,
        int totalDistanceM,
        int totalValue) {

    public record StopTime(Visit visit, int arrivalMin, int startMin, int departureMin) {
    }

    public record DayResult(
            int dayIndex,
            List<StopTime> stops,
            Lodging lodging,
            int travelMin,
            int distanceM,
            int endMin,
            int lateMin,
            int overtimeMin,
            int costKgs) {
    }

    public static TripSchedule of(List<TripDay> unsortedDays) {
        List<TripDay> days = new ArrayList<>(unsortedDays);
        days.sort(Comparator.comparing(TripDay::getIndex));
        TripSettings settings = days.get(0).getSettings();
        TravelMatrix matrix = settings.matrix();

        List<DayResult> results = new ArrayList<>(days.size());
        Lodging currentLodging = settings.startLodging();
        int late = 0;
        int overtime = 0;
        int noLodging = 0;
        int cost = 0;
        int travel = 0;
        int distance = 0;
        int value = 0;

        for (TripDay day : days) {
            int time = settings.pace().dayStartMin();
            int location = currentLodging.locationIndex();
            int dayTravel = 0;
            int dayDistance = 0;
            int dayLate = 0;
            int dayCost = 0;
            List<StopTime> stops = new ArrayList<>(day.getVisits().size());

            for (Visit visit : day.getVisits()) {
                int leg = matrix.minutes(location, visit.getLocationIndex());
                dayTravel += leg;
                dayDistance += matrix.meters(location, visit.getLocationIndex());
                int arrival = time + leg;
                int start = Math.max(arrival, visit.getOpensMin());
                int departure = start + visit.getDurationMin();
                if (departure > visit.getClosesMin()) {
                    dayLate += departure - visit.getClosesMin();
                }
                stops.add(new StopTime(visit, arrival, start, departure));
                time = departure;
                location = visit.getLocationIndex();
                dayCost += visit.getCostKgs();
                value += visit.getValue();
            }

            Lodging night = null;
            int target;
            if (day.isLast()) {
                target = settings.endLocationIndex();
            } else {
                if (day.getVisits().isEmpty()) {
                    night = currentLodging;
                } else {
                    Visit last = day.getVisits().get(day.getVisits().size() - 1);
                    night = last.getNearestLodging();
                    if (night == null) {
                        noLodging++;
                        night = Lodging.campAt(last.getLocationIndex());
                    }
                }
                target = night.locationIndex();
                dayCost += night.priceKgs();
            }
            int finalLeg = matrix.minutes(location, target);
            dayTravel += finalLeg;
            dayDistance += matrix.meters(location, target);
            time += finalLeg;
            dayCost += (int) Math.round(dayDistance / 1000.0 * settings.transportCostPerKm());

            int dayOvertime = Math.max(0, time - settings.pace().dayEndMin());
            results.add(new DayResult(day.getIndex(), stops, night, dayTravel, dayDistance, time,
                    dayLate, dayOvertime, dayCost));

            late += dayLate;
            overtime += dayOvertime;
            cost += dayCost;
            travel += dayTravel;
            distance += dayDistance;
            if (night != null) {
                currentLodging = night;
            }
        }

        int overBudget = Math.max(0, cost - settings.budgetKgs());
        return new TripSchedule(List.copyOf(results), late, overtime, noLodging, cost, overBudget,
                travel, distance, value);
    }

    public boolean isTimelineFeasible() {
        return lateMinutes == 0 && overtimeMinutes == 0 && nightsWithoutLodging == 0 && overBudgetKgs == 0;
    }
}
