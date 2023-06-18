package com.github.jikoo.planarwrappers.util.version;

import org.jetbrains.annotations.NotNull;

class IntVersion implements Version {

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
      return StringVersion.COMPARATOR.compare(this.toString(), other.toString());
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
