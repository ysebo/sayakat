package kg.alatoo.sayakat.poi;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kg.alatoo.sayakat.common.Errors;
import kg.alatoo.sayakat.common.Geo;

@Service
public class PoiService {

    private final PoiRepository pois;

    public PoiService(PoiRepository pois) {
        this.pois = pois;
    }

    @Transactional(readOnly = true)
    public List<PoiDto> list(PoiCategory category) {
        List<Poi> result = category == null
                ? pois.findAll(Sort.by(Sort.Order.desc("popularity"), Sort.Order.asc("name")))
                : pois.findByCategoryOrderByName(category);
        return result.stream().map(PoiDto::of).toList();
    }

    @Transactional(readOnly = true)
    public List<PoiDto> inBox(double minLat, double minLon, double maxLat, double maxLon, PoiCategory category) {
        return pois.findInBox(minLat, minLon, maxLat, maxLon, category == null ? null : category.name())
                .stream().map(PoiDto::of).toList();
    }

    @Transactional(readOnly = true)
    public PoiDto get(Long id) {
        return PoiDto.of(find(id));
    }

    @Transactional
    public PoiDto create(PoiDto dto) {
        Poi poi = new Poi();
        apply(poi, dto);
        return PoiDto.of(pois.save(poi));
    }

    @Transactional
    public PoiDto update(Long id, PoiDto dto) {
        Poi poi = find(id);
        apply(poi, dto);
        return PoiDto.of(poi);
    }

    @Transactional
    public void delete(Long id) {
        pois.delete(find(id));
    }

    Poi find(Long id) {
        return pois.findById(id).orElseThrow(() -> Errors.notFound("POI", id));
    }

    static void apply(Poi poi, PoiDto dto) {
        poi.setExternalId(dto.externalId());
        poi.setName(dto.name());
        poi.setDescription(dto.description());
        poi.setCategory(dto.category());
        poi.setTags(dto.tags() == null ? List.of() : List.copyOf(dto.tags()));
        poi.setRegion(dto.region());
        poi.setLocation(Geo.point(dto.lat(), dto.lon()));
        poi.setVisitDurationMin(dto.visitDurationMin());
        poi.setCostKgs(dto.costKgs());
        poi.setOpensAt(dto.opensAt());
        poi.setClosesAt(dto.closesAt());
        poi.setSeasonFrom((short) dto.seasonFrom());
        poi.setSeasonTo((short) dto.seasonTo());
        poi.setPopularity((short) dto.popularity());
        poi.setImageUrl(dto.imageUrl());
        poi.setSource(dto.source());
    }
}
