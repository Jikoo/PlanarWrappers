package com.github.jikoo.planarwrappers.tuple;

import org.jetbrains.annotations.NotNull;

/**
 * A data holder for three values.
 *
 * @param <L> the type of the left value
 * @param <M> the type of the middle value
 * @param <R> the type of the right value
 */
public class Triple<L, M, R> extends Pair<L, R> {

  private M middle;

  /**
   * Constructor for a Triple.
   *
   * @param left the left value
   * @param middle the middle value
   * @param right the right value
   */
  public Triple(@NotNull L left, @NotNull M middle, @NotNull R right) {
    super(left, right);
    this.middle = middle;
  }

  /**
   * Get the middle value.
   *
   * @return the middle value
   */
  public @NotNull M getMiddle() {
    return middle;
  }

  /**
   * Set the middle value.
   *
   * @param middle the new middle value
   */
  public void setMiddle(@NotNull M middle) {
    this.middle = middle;
  }

  @Override
  public int hashCode() {
    return super.hashCode() * 17 * middle.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj)
        && obj instanceof Triple<?, ?, ?> triple
        && middle.equals(triple.middle);
  }
}
