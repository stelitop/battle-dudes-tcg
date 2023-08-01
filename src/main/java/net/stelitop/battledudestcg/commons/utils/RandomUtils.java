package net.stelitop.battledudestcg.commons.utils;

import net.stelitop.battledudestcg.game.enums.Rarity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Random;

@Service
public class RandomUtils {

    @Autowired
    private Random random;

    /**
     * Randomly chooses an element from a list, where each element is weighted.
     * There must be at least one element in the list and the weights must be
     * positive.
     *
     * @param weights A collection of element-weight pairs.
     * @param <T> The type of the return element.
     * @return The randomly selected element.
     * @throws IllegalArgumentException Thrown if the list of weights is empty or
     * not all weights are positive.
     */
    public <T> T pickRandomlyWithWeights(@NonNull Collection<Pair<T, Float>> weights) throws IllegalArgumentException {
        if (weights.isEmpty()) throw new IllegalArgumentException("The weights list must not be empty.");
        if (weights.stream().anyMatch(x -> x.getSecond() <= 0)) throw new IllegalArgumentException("There cannot be non-positive weights");
        double totalSum = weights.stream().mapToDouble(Pair::getSecond).sum();
        double curSum = 0;
        double choice = random.nextDouble(totalSum);
        for (var weight : weights) {
            curSum += weight.getSecond();
            if (choice < curSum) return weight.getFirst();
        }
        return null;
    }

    /**
     * Returns a random rarity. The rarities are weighted in relation to dude
     * and item appearances. The weights are:
     * Common - 25/55
     * Rare - 16/55
     * Epic - 9/55
     * Legendary - 4/55
     * Mythic - 1/55
     *
     * @return A random rarity.
     */
    public Rarity pickWeightedRandomRarity() {
        return pickRandomlyWithWeights(List.of(
                Pair.of(Rarity.Common, 25f),
                Pair.of(Rarity.Rare, 16f),
                Pair.of(Rarity.Epic, 9f),
                Pair.of(Rarity.Legendary, 4f),
                Pair.of(Rarity.Mythic, 1f)
        ));
    }

    /**
     * Simulates an event with a probability p to succeed.
     *
     * @param probability Probability between 0 and 1.
     * @return True if the event succeeds, false otherwise.
     */
    public boolean invokeProbability(double probability) {
        return random.nextDouble(1) <= probability;
    }

    /**
     * Gets a random object from a list of objects.
     *
     * @param list The list of objects. Passing null is treated as an empty list.
     * @return A randomly selected object from the list. If the list is empty,
     *     null is returned instead.
     * @param <T> The generic type of the objects in the list.
     */
    public <T> @Nullable T getRandomItem(@Nullable List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(random.nextInt(list.size()));
    }
}
