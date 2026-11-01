package kg.alatoo.sayakat.planner.domain;

public enum Pace {
    RELAXED(2, 9 * 60, 18 * 60),
    MODERATE(3, 9 * 60, 19 * 60),
    INTENSE(5, 8 * 60, 20 * 60);

    private final int maxVisitsPerDay;
    private final int dayStartMin;
    private final int dayEndMin;

    Pace(int maxVisitsPerDay, int dayStartMin, int dayEndMin) {
        this.maxVisitsPerDay = maxVisitsPerDay;
        this.dayStartMin = dayStartMin;
        this.dayEndMin = dayEndMin;
    }

    public int maxVisitsPerDay() {
        return maxVisitsPerDay;
    }

    public int dayStartMin() {
        return dayStartMin;
    }

    public int dayEndMin() {
        return dayEndMin;
    }
}
