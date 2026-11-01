package kg.alatoo.sayakat.planner;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import kg.alatoo.sayakat.marketplace.ServiceOfferingRepository;
import kg.alatoo.sayakat.planner.domain.TripDay;
import kg.alatoo.sayakat.planner.domain.TripSolution;
import kg.alatoo.sayakat.planner.persistence.TripPlan;
import kg.alatoo.sayakat.planner.persistence.TripPlanDay;
import kg.alatoo.sayakat.planner.persistence.TripPlanRepository;
import kg.alatoo.sayakat.planner.persistence.TripPlanStop;
import kg.alatoo.sayakat.planner.solver.TripSchedule;
import kg.alatoo.sayakat.poi.PoiRepository;

@Component
public class PlanResultWriter {

    private final TripPlanRepository plans;
    private final PoiRepository pois;
    private final ServiceOfferingRepository services;

    public PlanResultWriter(TripPlanRepository plans, PoiRepository pois, ServiceOfferingRepository services) {
        this.plans = plans;
        this.pois = pois;
        this.services = services;
    }

    @Transactional
    public void saveSolution(Long planId, TripSolution solution, HardMediumSoftScore score) {
        TripPlan plan = plans.findById(planId).orElseThrow();
        TripSchedule schedule = TripSchedule.of(solution.getDays());

        plan.getItinerary().clear();
        for (TripDay day : solution.getDays()) {
            TripSchedule.DayResult result = schedule.days().get(day.getIndex());
            TripPlanDay planDay = new TripPlanDay();
            planDay.setPlan(plan);
            planDay.setDayIndex(day.getIndex());
            planDay.setDate(day.getDate());
            planDay.setTravelMin(result.travelMin());
            planDay.setCostKgs(result.costKgs());
            planDay.setEndMin(result.endMin());
            if (result.lodging() != null) {
                planDay.setLodgingName(result.lodging().name());
                if (result.lodging().serviceId() != null) {
                    planDay.setLodgingService(services.getReferenceById(result.lodging().serviceId()));
                }
            }
            int seq = 0;
            for (TripSchedule.StopTime stopTime : result.stops()) {
                TripPlanStop stop = new TripPlanStop();
                stop.setDay(planDay);
                stop.setSeq(seq++);
                stop.setPoi(pois.getReferenceById(stopTime.visit().getId()));
                stop.setArrivalMin(stopTime.arrivalMin());
                stop.setDepartureMin(stopTime.departureMin());
                planDay.getStops().add(stop);
            }
            plan.getItinerary().add(planDay);
        }

        plan.setScore(score.toString());
        plan.setFeasible(score.isFeasible());
        plan.setTotalCostKgs(schedule.totalCostKgs());
        plan.setTotalTravelMin(schedule.totalTravelMin());
        plan.setTotalValue(schedule.totalValue());
        plan.setStatus(TripPlan.Status.SOLVED);
        plan.setSolvedAt(Instant.now());
    }

    @Transactional
    public void saveFailure(Long planId, Throwable error) {
        plans.findById(planId).ifPresent(plan -> {
            plan.setStatus(TripPlan.Status.FAILED);
            plan.setError(error.getClass().getSimpleName() + ": " + error.getMessage());
            plan.setSolvedAt(Instant.now());
        });
    }
}
