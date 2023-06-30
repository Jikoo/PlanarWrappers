package com.github.jikoo.planarwrappers.util.version;

import java.util.regex.Pattern;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A utility for declaring and comparing versions.
 *
 * <p>While this class does have the ability to compare raw semantic versions (e.g.
 * {@code 1.5.0 < 1.6.0}) it does not handle snapshot data according to the SemVer spec, and it will
 * compare metadata. It is intended to be used to compare a specific version of a dependency against
 * the version that is currently present, so the version's format should be known and consistent.
 *
 * <p>This class has no ability to compare commit hashes.
 */
public interface Version extends Comparable<Version> {

  /**
   * Calculate whether this {@code Version} is earlier than another {@code Version}.
   *
   * @param other the other {@code Version}
   * @return true if this {@code Version} is earlier
   */
  default boolean lessThan(@NotNull Version other) {
    return compareTo(other) < 0;
  }

  /**
   * Calculate whether this {@code Version} is earlier than or equal to another {@code Version}.
   *
   * @param other the other {@code Version}
   * @return true if this {@code Version} is earlier or equal
   */
  default boolean lessThanOrEqual(@NotNull Version other) {
    return compareTo(other) <= 0;
  }

  /**
   * Calculate whether this {@code Version} is later than another {@code Version}.
   *
   * @param other the other {@code Version}
   * @return true if this {@code Version} is later
   */
  default boolean greaterThan(@NotNull Version other) {
    return compareTo(other) > 0;
  }

  /**
   * Calculate whether this {@code Version} is later than or equal to another {@code Version}.
   *
   * @param other the other {@code Version}
   * @return true if this {@code Version} is later or equal
   */
  default boolean greaterThanOrEqual(@NotNull Version other) {
    return compareTo(other) >= 0;
  }

  /**
   * Create a new integer-based {@code Version}.
   *
   * @param data an array of integers
   * @return the created {@code Version}
   */
  @Contract("_ -> new")
  static @NotNull Version of(int... data) {
    return new IntVersion(data);
  }

  /**
   * Create a new {@code Version}.
   *
   * @param data the string of versioning data
   * @return the created {@link Version}
   */
  @Contract("_ -> new")
  static @NotNull Version of(@NotNull String data) {
    if (Pattern.compile("^(\\d+\\.)*+\\d+$").matcher(data).matches()) {
      String[] raw = data.split("\\.");
      int[] parsed = new int[raw.length];
      for (int index = 0; index < raw.length; index++) {
        parsed[index] = Integer.parseInt(raw[index]);
      }
      return new IntVersion(parsed);
    }
    return new StringVersion(data);
  }

}
