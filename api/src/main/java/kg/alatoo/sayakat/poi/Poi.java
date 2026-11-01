package kg.alatoo.sayakat.poi;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "poi")
@Getter
@Setter
public class Poi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PoiCategory category;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]", nullable = false)
    private List<String> tags = new ArrayList<>();

    private String region;

    @Column(nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point location;

    @Column(name = "visit_duration_min", nullable = false)
    private int visitDurationMin;

    @Column(name = "cost_kgs", nullable = false)
    private int costKgs;

    @Column(name = "opens_at")
    private LocalTime opensAt;

    @Column(name = "closes_at")
    private LocalTime closesAt;

    @Column(name = "season_from", nullable = false)
    private short seasonFrom = 1;

    @Column(name = "season_to", nullable = false)
    private short seasonTo = 12;

    @Column(nullable = false)
    private short popularity = 3;

    @Column(name = "image_url")
    private String imageUrl;

    private String source;

    public double lat() {
        return location.getY();
    }

    public double lon() {
        return location.getX();
    }
}
