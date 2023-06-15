package com.github.jikoo.planarwrappers.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.google.common.reflect.TypeToken;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class AlphanumComparatorTest {

  @Test
  void sort() {
    // Assemble sorted values.
    List<String> sorted = Arrays.asList(
        "01DELTA.A", "01DELTA.B",
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

  @Test
  void sortIgnoreCase() {
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

    values.sort(new AlphanumComparator(String.CASE_INSENSITIVE_ORDER));

    assertThat("Contents are ordered properly", values, is(sorted));
  }

  @Test
  void sortCollator() {
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

    // This is more of a proof-of-concept than a good idea: sorting using a Collator and not caching
    // keys is painfully slow, over twice as slow as the normal case-insensitive sort above.
    // You'd be far better off storing your own normalized-for-sorting version and sorting off that.
    // However, for a quick(-to-type) and dirty hack, one can wrangle a Collator into working.
    Collator collator = Collator.getInstance(Locale.ROOT);
    collator.setStrength(Collator.PRIMARY);
    @SuppressWarnings("unchecked")
    Comparator<String> comparator = (Comparator<String>) TypeToken.of(Collator.class).getRawType().cast(collator);
    values.sort(new AlphanumComparator(comparator));

    assertThat("Contents are ordered properly", values, is(sorted));
  }

}
