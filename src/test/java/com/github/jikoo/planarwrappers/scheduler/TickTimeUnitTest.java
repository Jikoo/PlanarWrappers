package com.github.jikoo.planarwrappers.scheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TickTimeUnitTest {

  @ParameterizedTest
  @CsvSource(value = { "50,1", "49,0", "500,10", "501,10" })
  void testMillisToTicks(long millis, long ticks) {
    assertThat(
        "Millis are converted to the expected number of ticks",
        TickTimeUnit.toTicks(millis, TimeUnit.MILLISECONDS),
        is(ticks));
  }

  @ParameterizedTest
  @CsvSource(value = { "1,50", "10,500", "2,100" })
  void testTicksToMillis(long ticks, long millis) {
    assertThat(
        "Ticks are converted to the expected number of millis",
        TickTimeUnit.toTime(ticks, TimeUnit.MILLISECONDS),
        is(millis));
  }

  @ParameterizedTest
  @CsvSource(value = { "20,1", "19,0", "1200,60" })
  void testTicksToSeconds(long ticks, long seconds) {
    assertThat(
        "Ticks are converted to the expected number of millis",
        TickTimeUnit.toTime(ticks, TimeUnit.SECONDS),
        is(seconds));
  }

}