package com.github.jikoo.planarwrappers.util;

import java.util.Collection;
import java.util.Random;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/** A utility for selecting a weighted choice randomly. */
public final class WeightedRandom {

  private WeightedRandom() {}

  /**
   * Choose an element from a collection of choices.
   *
   * @param random the Random to use
   * @param choices the choices
   * @param <T> the type of choice
   * @return the selected choice
   */
  public static <T extends Choice> @NotNull T choose(Random random, Collection<T> choices) {
    int choiceMax = sum(choices);

    if (choiceMax <= 0) {
      throw new IllegalArgumentException("Must provide at least 1 choice with weight!");
    }

    int chosen = random.nextInt(choiceMax);

    for (T choice : choices) {
      chosen -= choice.getWeight();
      if (chosen <= 0) {
        return choice;
      }
    }

    throw new IllegalStateException(
        "Generated an index out of bounds with " + random.getClass().getName());
  }

  private static int sum(@NotNull Collection<? extends Choice> choices) {
    return choices.stream().mapToInt(Choice::getWeight).sum();
  }

  /** A weighted random choice. Chance of selection is based on total choices and weights. */
  public interface Choice {

    /**
     * Get the weight of the Choice.
     *
     * @return the weight of the choice
     */
    @Range(from = 1, to = Integer.MAX_VALUE)
    int getWeight();
  }
}
