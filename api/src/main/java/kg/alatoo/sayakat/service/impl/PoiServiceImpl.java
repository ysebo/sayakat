package kg.alatoo.sayakat.service.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kg.alatoo.sayakat.dto.PoiDto;
import kg.alatoo.sayakat.entity.Poi;
import kg.alatoo.sayakat.entity.enums.PoiCategory;
import kg.alatoo.sayakat.exception.Errors;
import kg.alatoo.sayakat.repository.PoiRepository;
import kg.alatoo.sayakat.service.PoiService;
import kg.alatoo.sayakat.util.Geo;

@Service
@RequiredArgsConstructor
public class PoiServiceImpl implements PoiService {

    private final PoiRepository poiRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PoiDto> list(PoiCategory category) {
        List<Poi> result = category == null
                ? poiRepository.findAll(Sort.by(Sort.Order.desc("popularity"), Sort.Order.asc("name")))
                : poiRepository.findByCategoryOrderByName(category);
        return result.stream().map(PoiDto::of).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PoiDto> inBox(double minLat, double minLon, double maxLat, double maxLon, PoiCategory category) {
        return poiRepository.findInBox(minLat, minLon, maxLat, maxLon, category == null ? null : category.name())
                .stream().map(PoiDto::of).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PoiDto get(Long id) {
        return PoiDto.of(find(id));
    }

    @Override
    @Transactional
    public PoiDto create(PoiDto dto) {
        Poi poi = new Poi();
        apply(poi, dto);
        return PoiDto.of(poiRepository.save(poi));
    }

    @Override
    @Transactional
    public PoiDto update(Long id, PoiDto dto) {
        Poi poi = find(id);
        apply(poi, dto);
        return PoiDto.of(poi);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        poiRepository.delete(find(id));
    }

    private Poi find(Long id) {
        return poiRepository.findById(id).orElseThrow(() -> Errors.notFound("POI", id));
    }

    private static void apply(Poi poi, PoiDto dto) {
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
