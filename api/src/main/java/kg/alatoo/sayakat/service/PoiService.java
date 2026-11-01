package kg.alatoo.sayakat.service;

import java.util.List;

import kg.alatoo.sayakat.dto.PoiDto;
import kg.alatoo.sayakat.entity.enums.PoiCategory;

public interface PoiService {

    List<PoiDto> list(PoiCategory category);

    List<PoiDto> inBox(double minLat, double minLon, double maxLat, double maxLon, PoiCategory category);

    PoiDto get(Long id);

    PoiDto create(PoiDto dto);

    PoiDto update(Long id, PoiDto dto);

    void delete(Long id);
}
