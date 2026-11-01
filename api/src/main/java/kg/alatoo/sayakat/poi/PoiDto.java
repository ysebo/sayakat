package kg.alatoo.sayakat.poi;

import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record PoiDto(
        Long id,
        @NotBlank String externalId,
        @NotBlank String name,
        String description,
        @NotNull PoiCategory category,
        List<String> tags,
        String region,
        @NotNull @Min(39) @Max(44) Double lat,
        @NotNull @Min(69) @Max(81) Double lon,
        @Positive int visitDurationMin,
        @PositiveOrZero int costKgs,
        LocalTime opensAt,
        LocalTime closesAt,
        @Min(1) @Max(12) int seasonFrom,
        @Min(1) @Max(12) int seasonTo,
        @Min(1) @Max(5) int popularity,
        String imageUrl,
        String source) {

    public static PoiDto of(Poi p) {
        return new PoiDto(p.getId(), p.getExternalId(), p.getName(), p.getDescription(), p.getCategory(), p.getTags(),
                p.getRegion(), p.lat(), p.lon(), p.getVisitDurationMin(), p.getCostKgs(), p.getOpensAt(),
                p.getClosesAt(), p.getSeasonFrom(), p.getSeasonTo(), p.getPopularity(), p.getImageUrl(), p.getSource());
    }
}
