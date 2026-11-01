package kg.alatoo.sayakat.dto.response;

import kg.alatoo.sayakat.entity.Poi;
import kg.alatoo.sayakat.entity.TripPlanStop;
import kg.alatoo.sayakat.util.TimeFormat;

public record StopResponse(int seq, Long poiId, String name, String category, double lat, double lon,
                           String arrival, String departure) {

    public static StopResponse of(TripPlanStop stop) {
        Poi poi = stop.getPoi();
        return new StopResponse(stop.getSeq(), poi.getId(), poi.getName(), poi.getCategory().name(), poi.lat(),
                poi.lon(), TimeFormat.clock(stop.getArrivalMin()), TimeFormat.clock(stop.getDepartureMin()));
    }
}
