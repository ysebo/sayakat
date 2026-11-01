package kg.alatoo.sayakat.planner.solver;

import java.util.Collection;
import java.util.Set;

public final class ValueModel {

    static final double BASE = 0.3;

    private ValueModel() {
    }

    public static int value(String category, Collection<String> tags, int popularity, Set<String> interests) {
        double match;
        if (interests.isEmpty() || interests.contains(category)) {
            match = 1.0;
        } else if (tags.stream().anyMatch(interests::contains)) {
            match = 0.5;
        } else {
            match = 0.0;
        }
        return (int) Math.round(100.0 * popularity / 5.0 * (BASE + (1 - BASE) * match));
    }
}
