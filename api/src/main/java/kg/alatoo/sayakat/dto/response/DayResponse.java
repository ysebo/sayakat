package kg.alatoo.sayakat.dto.response;

import java.time.LocalDate;
import java.util.List;

import kg.alatoo.sayakat.entity.ServiceOffering;
import kg.alatoo.sayakat.entity.TripPlanDay;
import kg.alatoo.sayakat.util.TimeFormat;

public record DayResponse(int dayIndex, LocalDate date, List<StopResponse> stops, Long lodgingServiceId,
                          String lodgingName, Double lodgingLat, Double lodgingLon, int travelMin, int costKgs,
                          String endsAt) {

    public static DayResponse of(TripPlanDay day) {
        ServiceOffering lodging = day.getLodgingService();
        boolean located = lodging != null && lodging.getLocation() != null;
        return new DayResponse(day.getDayIndex(), day.getDate(), day.getStops().stream().map(StopResponse::of).toList(),
                lodging == null ? null : lodging.getId(), day.getLodgingName(),
                located ? lodging.getLocation().getY() : null, located ? lodging.getLocation().getX() : null,
                day.getTravelMin(), day.getCostKgs(), TimeFormat.clock(day.getEndMin()));
    }
}
