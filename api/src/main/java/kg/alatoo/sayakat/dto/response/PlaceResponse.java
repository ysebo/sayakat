package kg.alatoo.sayakat.dto.response;

import kg.alatoo.sayakat.entity.Poi;

public record PlaceResponse(Long id, String name, double lat, double lon) {

    public static PlaceResponse of(Poi poi) {
        return new PlaceResponse(poi.getId(), poi.getName(), poi.lat(), poi.lon());
    }
}
