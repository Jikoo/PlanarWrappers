package com.github.jikoo.planarwrappers.util.version;

import com.github.jikoo.planarwrappers.util.AlphanumComparator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class StringVersion implements Version {

  static final AlphanumComparator COMPARATOR = new AlphanumComparator() {
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
    return COMPARATOR.compare(this.versionText, that.toString());
  }

}
