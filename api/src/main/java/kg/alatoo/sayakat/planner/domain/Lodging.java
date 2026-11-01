package kg.alatoo.sayakat.planner.domain;

public record Lodging(Long serviceId, String name, int locationIndex, int priceKgs, boolean camp) {

    public static Lodging of(Long serviceId, String name, int locationIndex, int priceKgs) {
        return new Lodging(serviceId, name, locationIndex, priceKgs, false);
    }

    public static Lodging campAt(int locationIndex) {
        return new Lodging(null, "No lodging nearby", locationIndex, 0, true);
    }
}
