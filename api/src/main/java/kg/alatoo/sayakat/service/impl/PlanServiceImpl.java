package kg.alatoo.sayakat.service.impl;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kg.alatoo.sayakat.config.properties.PlannerProperties;
import kg.alatoo.sayakat.dto.request.PlanRequest;
import kg.alatoo.sayakat.dto.response.PlanResponse;
import kg.alatoo.sayakat.dto.response.ServiceResponse;
import kg.alatoo.sayakat.entity.Poi;
import kg.alatoo.sayakat.entity.ServiceOffering;
import kg.alatoo.sayakat.entity.TripPlan;
import kg.alatoo.sayakat.entity.TripPlanDay;
import kg.alatoo.sayakat.entity.enums.PlanAlgorithm;
import kg.alatoo.sayakat.entity.enums.PlanStatus;
import kg.alatoo.sayakat.entity.enums.ServiceType;
import kg.alatoo.sayakat.exception.Errors;
import kg.alatoo.sayakat.planner.domain.Lodging;
import kg.alatoo.sayakat.planner.domain.TripSolution;
import kg.alatoo.sayakat.planner.greedy.GreedyPlanner;
import kg.alatoo.sayakat.planner.routing.GeoPoint;
import kg.alatoo.sayakat.planner.routing.RoutingNode;
import kg.alatoo.sayakat.planner.routing.TravelMatrix;
import kg.alatoo.sayakat.planner.solver.TripProblemFactory.CandidateLodging;
import kg.alatoo.sayakat.planner.solver.TripProblemFactory.CandidatePoi;
import kg.alatoo.sayakat.planner.solver.TripProblemFactory.TripRequest;
import kg.alatoo.sayakat.planner.solver.TripProblemFactory;
import kg.alatoo.sayakat.repository.PoiRepository;
import kg.alatoo.sayakat.repository.ServiceOfferingRepository;
import kg.alatoo.sayakat.repository.TripPlanRepository;
import kg.alatoo.sayakat.repository.UserRepository;
import kg.alatoo.sayakat.service.PlanResultService;
import kg.alatoo.sayakat.service.PlanService;
import kg.alatoo.sayakat.service.TravelMatrixService;
import kg.alatoo.sayakat.util.Geo;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final TripPlanRepository tripPlanRepository;
    private final PoiRepository poiRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final UserRepository userRepository;
    private final TravelMatrixService travelMatrixService;
    private final PlanResultService planResultService;
    private final SolverManager<TripSolution, Long> solverManager;
    private final SolutionManager<TripSolution, HardMediumSoftScore> solutionManager;
    private final PlannerProperties properties;

    @Override
    public Long create(Long userId, PlanRequest request) {
        TripPlan plan = createPlan(userId, request);
        TripSolution problem = buildProblem(plan);
        Long planId = plan.getId();

        TripSolution warmStart = GreedyPlanner.solve(problem);
        if (plan.getAlgorithm() == PlanAlgorithm.GREEDY) {
            planResultService.saveSolution(planId, warmStart, solutionManager.update(warmStart));
        } else {
            solverManager.solveBuilder()
                    .withProblemId(planId)
                    .withProblem(warmStart)
                    .withFinalBestSolutionConsumer(solution ->
                            planResultService.saveSolution(planId, solution, solution.getScore()))
                    .withExceptionHandler((id, error) -> {
                        log.error("Solving plan {} failed", id, error);
                        planResultService.saveFailure(id, error);
                    })
                    .run();
        }
        return planId;
    }

    @Override
    @Transactional(readOnly = true)
    public PlanResponse get(Long userId, Long planId) {
        return PlanResponse.of(owned(userId, planId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponse> findByUser(Long userId) {
        return tripPlanRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(PlanResponse::of).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> servicesForDay(Long userId, Long planId, int dayIndex, ServiceType type,
                                                double radiusKm) {
        TripPlan plan = owned(userId, planId);
        TripPlanDay day = plan.getItinerary().stream().filter(d -> d.getDayIndex() == dayIndex).findFirst()
                .orElseThrow(() -> Errors.notFound("Day", dayIndex));
        List<GeoPoint> anchors = new ArrayList<>();
        if (day.getLodgingService() != null && day.getLodgingService().getLocation() != null) {
            anchors.add(Geo.toGeoPoint(day.getLodgingService().getLocation()));
        }
        day.getStops().stream().map(s -> Geo.toGeoPoint(s.getPoi().getLocation())).forEach(anchors::add);
        if (anchors.isEmpty()) {
            anchors.add(Geo.toGeoPoint(plan.getStartPoi().getLocation()));
        }
        Map<Long, ServiceOffering> unique = new HashMap<>();
        for (GeoPoint point : anchors) {
            serviceOfferingRepository.findNear(point.lat(), point.lon(), radiusKm * 1000,
                    type == null ? null : type.name()).forEach(s -> unique.putIfAbsent(s.getId(), s));
        }
        return unique.values().stream().map(ServiceResponse::of).toList();
    }

    private TripPlan createPlan(Long userId, PlanRequest request) {
        Poi start = poiRepository.findById(request.startPoiId())
                .orElseThrow(() -> Errors.notFound("POI", request.startPoiId()));
        Long endId = request.endPoiId() == null ? request.startPoiId() : request.endPoiId();
        Poi end = poiRepository.findById(endId).orElseThrow(() -> Errors.notFound("POI", endId));

        TripPlan plan = new TripPlan();
        plan.setUser(userRepository.getReferenceById(userId));
        plan.setStatus(PlanStatus.SOLVING);
        plan.setAlgorithm(request.algorithm() == null ? PlanAlgorithm.TIMEFOLD : request.algorithm());
        plan.setStartDate(request.startDate());
        plan.setDays(request.days());
        plan.setBudgetKgs(request.budgetKgs());
        plan.setPace(request.pace());
        plan.setInterests(request.interests() == null ? List.of()
                : request.interests().stream().map(Enum::name).sorted().toList());
        plan.setStartPoi(start);
        plan.setEndPoi(end);
        return tripPlanRepository.save(plan);
    }

    private TripSolution buildProblem(TripPlan plan) {
        Long startId = plan.getStartPoi().getId();
        Long endId = plan.getEndPoi().getId();
        List<Poi> candidates = poiRepository.findAll().stream()
                .filter(p -> !p.getId().equals(startId) && !p.getId().equals(endId))
                .toList();
        List<ServiceOffering> lodgings = serviceOfferingRepository.findByTypeAndActiveTrue(ServiceType.ACCOMMODATION)
                .stream()
                .filter(s -> s.getLocation() != null)
                .toList();

        List<RoutingNode> nodes = new ArrayList<>();
        candidates.forEach(p -> nodes.add(new RoutingNode("poi:" + p.getId(), Geo.toGeoPoint(p.getLocation()))));
        lodgings.forEach(s -> nodes.add(new RoutingNode("svc:" + s.getId(), Geo.toGeoPoint(s.getLocation()))));
        nodes.add(new RoutingNode("poi:" + startId, Geo.toGeoPoint(plan.getStartPoi().getLocation())));
        int startIndex = nodes.size() - 1;
        int endIndex = startIndex;
        if (!endId.equals(startId)) {
            nodes.add(new RoutingNode("poi:" + endId, Geo.toGeoPoint(plan.getEndPoi().getLocation())));
            endIndex = nodes.size() - 1;
        }
        TravelMatrix matrix = travelMatrixService.matrixFor(nodes);

        List<CandidatePoi> candidatePois = new ArrayList<>(candidates.size());
        for (int i = 0; i < candidates.size(); i++) {
            Poi p = candidates.get(i);
            candidatePois.add(new CandidatePoi(p.getId(), p.getName(), p.getCategory().name(), p.getTags(), i,
                    p.getVisitDurationMin(), minutes(p.getOpensAt(), 0),
                    minutes(p.getClosesAt(), TripProblemFactory.MINUTES_PER_DAY), p.getCostKgs(),
                    p.getSeasonFrom(), p.getSeasonTo(), p.getPopularity()));
        }
        List<CandidateLodging> candidateLodgings = new ArrayList<>(lodgings.size());
        for (int i = 0; i < lodgings.size(); i++) {
            ServiceOffering s = lodgings.get(i);
            candidateLodgings.add(new CandidateLodging(s.getId(), s.getTitle(), candidates.size() + i, s.getPriceKgs()));
        }

        Set<String> interests = plan.getInterests().stream().collect(Collectors.toUnmodifiableSet());
        TripRequest request = new TripRequest(plan.getStartDate(), plan.getDays(), plan.getPace(), plan.getBudgetKgs(),
                interests);
        Lodging startLodging = Lodging.of(null, "Hotel in " + plan.getStartPoi().getName(), startIndex,
                properties.defaultCityLodgingPrice());
        return TripProblemFactory.build(request, candidatePois, candidateLodgings, startLodging, endIndex,
                properties.transportCostPerKm(), properties.maxLodgingTransferMin(), matrix);
    }

    private TripPlan owned(Long userId, Long planId) {
        TripPlan plan = tripPlanRepository.findById(planId).orElseThrow(() -> Errors.notFound("Plan", planId));
        if (!plan.getUser().getId().equals(userId)) {
            throw Errors.notFound("Plan", planId);
        }
        return plan;
    }

    private static int minutes(LocalTime time, int fallback) {
        return time == null ? fallback : time.getHour() * 60 + time.getMinute();
    }
}
