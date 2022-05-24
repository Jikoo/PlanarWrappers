package com.github.jikoo.planarwrappers.util;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link Clock} implementation that reports fake times based on an internally stored number. Fake
 * times are in milliseconds.
 */
public class MockClock extends Clock {

  private final @NotNull AtomicLong now;

  /**
   * Construct a new fake clock starting at 1970-01-01T00:00:00Z.
   */
  public MockClock() {
    this(0L);
  }

  /**
   * Construct a new fake clock starting at a specified time.
   *
   * @param now the number of milliseconds since 1970-01-01T00:00:00Z
   */
  public MockClock(long now) {
    this.now = new AtomicLong(now);
  }

  /**
   * Add a number of milliseconds to the fake time.
   *
   * @param value the number of milliseconds to add
   */
  public void add(long value) {
    now.addAndGet(value);
  }

  /**
   * Add an amount of time to the fake clock. Times are internally stored in milliseconds, more
   * granular additions may be lost.
   *
   * @param value the value to add
   * @param timeUnit the TimeUnit of the value
   */
  public void add(long value, @NotNull TimeUnit timeUnit) {
    now.addAndGet(TimeUnit.MILLISECONDS.convert(value, timeUnit));
  }

  @Override
  public @NotNull ZoneId getZone() {
    return ZoneOffset.UTC;
  }

  @Override
  public @NotNull Clock withZone(ZoneId zoneId) {
    throw new UnsupportedOperationException("Cannot set timezone of fake clock.");
  }

  @Override
  public @NotNull Instant instant() {
    return Instant.ofEpochMilli(now.get());
  }

}
