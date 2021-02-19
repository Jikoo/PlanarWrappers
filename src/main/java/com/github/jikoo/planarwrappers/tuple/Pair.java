package com.github.jikoo.planarwrappers.tuple;

import org.jetbrains.annotations.NotNull;

/**
 * A data holder for two values.
 *
 * @param <L> the type of the left value
 * @param <R> the type of the right value
 */
public class Pair<L, R> {

  private L left;
  private R right;

  /**
   * Constructor for a Pair.
   *
   * @param left the left value
   * @param right the right value
   */
  public Pair(@NotNull L left, @NotNull R right) {
    this.left = left;
    this.right = right;
  }

  /**
   * Get the left value.
   *
   * @return the left value
   */
  public @NotNull L getLeft() {
    return left;
  }

  /**
   * Set the left value.
   *
   * @param left the new left value
   */
  public void setLeft(@NotNull L left) {
    this.left = left;
  }

  /**
   * Get the right value.
   *
   * @return the right value
   */
  public @NotNull R getRight() {
    return right;
  }

  /**
   * Set the right value.
   *
   * @param right the new right value
   */
  public void setRight(@NotNull R right) {
    this.right = right;
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * 17 * left.hashCode();
    hash = hash * 17 * right.hashCode();
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Pair)) {
      return false;
    }
    Pair<?, ?> other = (Pair<?, ?>) obj;
    return left.equals(other.left) && right.equals(other.right);
  }
}
