package com.github.jikoo.planarwrappers.util.version;

import com.github.jikoo.planarwrappers.util.AlphanumComparator;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
public abstract class Version implements Comparable<Version> {

  /**
   * Default version comparator.
   */
  public static final AlphanumComparator DEFAULT_COMPARATOR = new AlphanumComparator() {
    @Override
    protected int compareTail(CompareData data1, CompareData data2) {
      if (data1.getMarker() < data1.getLength()) {
        return compareSnapshot(data1, data2);
      } else if (data2.getMarker() < data2.getLength()) {
        return -compareSnapshot(data2, data1);
      }
      return 0;
    }

    private int compareSnapshot(CompareData longer, CompareData shorter) {
      if (shorter.getLength() == 0) {
        // An empty string is the smallest version possible.
        return 1;
      }
      if (!isDigit(shorter.getString().charAt(shorter.getLength() - 1))
          || shorter.getString().indexOf('-') != -1) {
        // If the shorter version is a snapshot, the longer snapshot version is later.
        return 1;
      }
      char nextChar = longer.getString().charAt(longer.getLength() - 1);
      // If the longer version is not a snapshot, the longer version is later.
      return isDigit(nextChar) || nextChar == '.' ? 1 : -1;
    }
  };

  /**
   * Calculate whether this {@code Version} is earlier than another {@code Version}.
   *
   * @param other the other {@code Version}
   * @return true if this {@code Version} is earlier
   */
  public boolean lessThan(@NotNull Version other) {
    return compareTo(other) < 0;
  }

  /**
   * Calculate whether this {@code Version} is earlier than or equal to another {@code Version}.
   *
   * @param other the other {@code Version}
   * @return true if this {@code Version} is earlier or equal
   */
  public boolean lessThanOrEqual(@NotNull Version other) {
    return compareTo(other) <= 0;
  }

  /**
   * Calculate whether this {@code Version} is later than another {@code Version}.
   *
   * @param other the other {@code Version}
   * @return true if this {@code Version} is later
   */
  public boolean greaterThan(@NotNull Version other) {
    return compareTo(other) > 0;
  }

  /**
   * Calculate whether this {@code Version} is later than or equal to another {@code Version}.
   *
   * @param other the other {@code Version}
   * @return true if this {@code Version} is later or equal
   */
  public boolean greaterThanOrEqual(@NotNull Version other) {
    return compareTo(other) >= 0;
  }

  /**
   * Create a new integer-based {@code Version}.
   *
   * @param data an array of integers
   * @return the created {@code Version}
   */
  @Contract("_ -> new")
  public static @NotNull Version of(int... data) {
    return new IntVersion(data);
  }

  /**
   * Create a new {@code Version}.
   *
   * @param data the string of versioning data
   * @return the created {@link Version}
   */
  @Contract("_ -> new")
  public static @NotNull Version of(@NotNull String data) {
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

  private static class IntVersion extends Version {

    private final int[] data;
    private String toString;

    IntVersion(int @NotNull ... data) {
      this.data = data;
    }

    @Override
    public boolean equals(Object obj) {
      return this == obj || obj instanceof Version that && this.compareTo(that) == 0;
    }

    @Override
    public int hashCode() {
      int hash = 1;
      for (int index = data.length - 1; index >= 0; --index) {
        if (hash == 1 && data[index] == 0) {
          // Skip trailing zeroes so that hashcode is consistent with equals.
          continue;
        }
        hash = hash * 67 + data[index];
      }
      return hash;
    }

    @Override
    public String toString() {
      if (toString != null) {
        return toString;
      }

      int maxLength = data.length - 1;
      StringBuilder builder = new StringBuilder();
      for (int index = 0; index <= maxLength; ++index) {
        builder.append(data[index]);
        if (index != maxLength) {
          builder.append('.');
        }
      }

      toString = builder.toString();
      return toString;
    }

    @Override
    public int compareTo(@NotNull Version other) {
      if (!(other instanceof IntVersion that)) {
        return DEFAULT_COMPARATOR.compare(this.toString(), other.toString());
      }

      int compareLen = Math.max(this.data.length, that.data.length);
      for (int index = 0; index < compareLen; ++index) {
        // Pad length with implied 0 out to length as necessary.
        int thisData = this.data.length > index ? this.data[index] : 0;
        int thatData = that.data.length > index ? that.data[index] : 0;
        int compare = thisData - thatData;
        if (compare != 0) {
          return compare;
        }
      }
      return 0;
    }

  }

  private static class StringVersion extends Version {

    private final @NotNull String versionText;

    StringVersion(@NotNull String version) {
      this.versionText = version;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      return this == obj || obj instanceof Version that && this.compareTo(that) == 0;
    }

    @Override
    public int hashCode() {
      return versionText.hashCode();
    }

    @Override
    public String toString() {
      return versionText;
    }

    @Override
    public int compareTo(@NotNull Version that) {
      return DEFAULT_COMPARATOR.compare(this.versionText, that.toString());
    }

  }

}
