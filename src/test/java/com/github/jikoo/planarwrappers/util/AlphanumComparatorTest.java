package com.github.jikoo.planarwrappers.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance(Lifecycle.PER_CLASS)
class AlphanumComparatorTest {

  @Test
  void sort() {
    // Assemble sorted values.
    List<String> sorted = Arrays.asList(
        "1DELTA.A", "01DELTA.A", "001DELTA.A", "01DELTA.B",
        "01delta.a", "01delta.b", "10delta.a", "10delta.b", "11delta.a",
        "alpha.beta.1", "beta.theta",
        "gamma0", "gamma1", "gamma2", "gamma10", "gamma20"
    );
    List<String> values = new ArrayList<>(sorted);

    // Mess up copy for sorting.
    values.sort(Comparator.naturalOrder());

    assertThat(
        "Collections have identical contents",
        values,
        both(everyItem(is(in(sorted)))).and(containsInAnyOrder(sorted.toArray())));
    assertThat("Contents are ordered differently", values, not(sorted));

    values.sort(new AlphanumComparator());

    assertThat("Contents are ordered properly", values, is(sorted));
  }

  @ParameterizedTest
  @MethodSource("getStringComparators")
  void sortCollator(Comparator<String> comparator) {
    // Assemble sorted values.
    List<String> sorted = Arrays.asList(
        "01DELTA.A", "01delta.a", "01DELTA.B", "01delta.b","10delta.a", "10delta.b", "11delta.a"
    );
    List<String> values = new ArrayList<>(sorted);

    // Mess up copy for sorting.
    values.sort(Comparator.naturalOrder());

    assertThat(
        "Collections have identical contents",
        values,
        both(everyItem(is(in(sorted)))).and(containsInAnyOrder(sorted.toArray())));
    assertThat("Contents are ordered differently", values, not(sorted));

    // This is more of a proof-of-concept than a good idea: converting to a string to sort is a
    // pretty big performance hit, as is using a collator without caching keys.
    AlphanumComparator alphaNum = new AlphanumComparator() {
      @Override
      protected int compareAlphabetic(StringBuilder thisChunk, StringBuilder thatChunk) {
        return comparator.compare(thisChunk.toString(), thatChunk.toString());
      }
    };
    values.sort(alphaNum);

    assertThat("Contents are ordered properly", values, is(sorted));
  }

  private @NotNull Collection<Comparator<?>> getStringComparators() {
    Collator collator = Collator.getInstance();
    collator.setStrength(Collator.PRIMARY);
    return Arrays.asList(String.CASE_INSENSITIVE_ORDER, collator);
  }

}
