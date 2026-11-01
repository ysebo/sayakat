package kg.alatoo.sayakat.service.impl;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import lombok.RequiredArgsConstructor;

import kg.alatoo.sayakat.entity.TripPlan;
import kg.alatoo.sayakat.entity.TripPlanDay;
import kg.alatoo.sayakat.entity.TripPlanStop;
import kg.alatoo.sayakat.entity.enums.PlanStatus;
import kg.alatoo.sayakat.planner.domain.TripDay;
import kg.alatoo.sayakat.planner.domain.TripSolution;
import kg.alatoo.sayakat.planner.solver.TripSchedule;
import kg.alatoo.sayakat.repository.PoiRepository;
import kg.alatoo.sayakat.repository.ServiceOfferingRepository;
import kg.alatoo.sayakat.repository.TripPlanRepository;
import kg.alatoo.sayakat.service.PlanResultService;

@Service
@RequiredArgsConstructor
public class PlanResultServiceImpl implements PlanResultService {

    private final TripPlanRepository tripPlanRepository;
    private final PoiRepository poiRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;

    @Override
    @Transactional
    public void saveSolution(Long planId, TripSolution solution, HardMediumSoftScore score) {
        TripPlan plan = tripPlanRepository.findById(planId).orElseThrow();
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
                    planDay.setLodgingService(serviceOfferingRepository.getReferenceById(result.lodging().serviceId()));
                }
            }
            int seq = 0;
            for (TripSchedule.StopTime stopTime : result.stops()) {
                TripPlanStop stop = new TripPlanStop();
                stop.setDay(planDay);
                stop.setSeq(seq++);
                stop.setPoi(poiRepository.getReferenceById(stopTime.visit().getId()));
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
        plan.setStatus(PlanStatus.SOLVED);
        plan.setSolvedAt(Instant.now());
    }

    @Override
    @Transactional
    public void saveFailure(Long planId, Throwable error) {
        tripPlanRepository.findById(planId).ifPresent(plan -> {
            plan.setStatus(PlanStatus.FAILED);
            plan.setError(error.getClass().getSimpleName() + ": " + error.getMessage());
            plan.setSolvedAt(Instant.now());
        });
    }
}
