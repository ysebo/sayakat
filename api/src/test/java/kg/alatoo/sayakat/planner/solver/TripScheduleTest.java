package kg.alatoo.sayakat.planner.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import kg.alatoo.sayakat.planner.domain.Lodging;
import kg.alatoo.sayakat.planner.domain.Pace;
import kg.alatoo.sayakat.planner.domain.TripDay;
import kg.alatoo.sayakat.planner.domain.TripSettings;
import kg.alatoo.sayakat.planner.domain.Visit;
import kg.alatoo.sayakat.routing.TravelMatrix;

class TripScheduleTest {

    static TravelMatrix matrix() {
        int[][] min = new int[4][4];
        int[][] m = new int[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i != j) {
                    min[i][j] = 60;
                    m[i][j] = 50_000;
                }
            }
        }
        min[2][3] = min[3][2] = 10;
        m[2][3] = m[3][2] = 5_000;
        return new TravelMatrix(min, m);
    }

    static final Lodging CITY = Lodging.of(null, "City hotel", 0, 2000);
    static final Lodging GUESTHOUSE = Lodging.of(7L, "Lake guesthouse", 3, 1500);

    static TripSettings settings(int days, int budget) {
        return new TripSettings(LocalDate.of(2026, 7, 1), days, Pace.MODERATE, budget, Set.of(), CITY, 0, 10, matrix());
    }

    static Visit museum() {
        return new Visit(1L, "Museum", 1, 90, 10 * 60, 12 * 60, 500, 1, 12, 50, CITY);
    }

    static Visit lake() {
        return new Visit(2L, "Lake", 2, 120, 0, 24 * 60, 0, 6, 9, 80, GUESTHOUSE);
    }

    static List<TripDay> days(TripSettings settings, List<List<Visit>> plan) {
        List<TripDay> days = new ArrayList<>();
        for (int i = 0; i < plan.size(); i++) {
            TripDay day = new TripDay(i, settings.startDate().plusDays(i), settings);
            day.setVisits(new ArrayList<>(plan.get(i)));
            day.getVisits().forEach(v -> v.setDay(day));
            days.add(day);
        }
        return days;
    }

    @Test
    void waitsForOpeningAndSleepsAtNearestLodging() {
        TripSettings s = settings(2, 100_000);
        TripSchedule schedule = TripSchedule.of(days(s, List.of(List.of(museum(), lake()), List.of())));

        var day1 = schedule.days().get(0);
        assertThat(day1.stops().get(0).arrivalMin()).isEqualTo(10 * 60);
        assertThat(day1.stops().get(0).departureMin()).isEqualTo(11 * 60 + 30);
        assertThat(day1.endMin()).isEqualTo(14 * 60 + 40);
        assertThat(day1.lodging()).isEqualTo(GUESTHOUSE);
        assertThat(schedule.lateMinutes()).isZero();

        var day2 = schedule.days().get(1);
        assertThat(day2.lodging()).isNull();
        assertThat(day2.travelMin()).isEqualTo(60);
        assertThat(schedule.isTimelineFeasible()).isTrue();
    }

    @Test
    void countsLatenessWhenVisitEndsAfterClosing() {
        TripSettings s = settings(1, 100_000);
        TripSchedule schedule = TripSchedule.of(days(s, List.of(List.of(lake(), museum()))));
        assertThat(schedule.lateMinutes()).isEqualTo(150);
        assertThat(schedule.isTimelineFeasible()).isFalse();
    }

    @Test
    void costIncludesEntranceLodgingAndTransport() {
        TripSettings s = settings(2, 3000);
        TripSchedule schedule = TripSchedule.of(days(s, List.of(List.of(museum(), lake()), List.of())));
        assertThat(schedule.totalCostKgs()).isEqualTo(500 + 1500 + 1550);
        assertThat(schedule.overBudgetKgs()).isEqualTo(550);
    }

    @Test
    void reportsNightWithoutLodging() {
        TripSettings s = settings(2, 100_000);
        Visit remote = new Visit(3L, "Remote pass", 2, 60, 0, 24 * 60, 0, 1, 12, 40, null);
        TripSchedule schedule = TripSchedule.of(days(s, List.of(List.of(remote), List.of())));
        assertThat(schedule.nightsWithoutLodging()).isEqualTo(1);
        assertThat(schedule.days().get(0).lodging().camp()).isTrue();
    }
}
