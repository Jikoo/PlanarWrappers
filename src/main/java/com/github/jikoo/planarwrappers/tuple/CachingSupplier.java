package com.github.jikoo.planarwrappers.tuple;

import java.time.Clock;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class CachingSupplier<T> implements Supplier<T> {

  private final @NotNull Supplier<T> supplier;
  private final long cacheDuration;
  private final @NotNull Clock clock;
  private T value;
  private long lastUpdate = Long.MIN_VALUE;

  public CachingSupplier(
      @NotNull Supplier<T> supplier,
      long duration,
      @NotNull TimeUnit timeUnit) {
    this(supplier, duration, timeUnit, Clock.systemUTC());
  }

  public CachingSupplier(
      @NotNull Supplier<T> supplier,
      @Range(from = 1, to = Long.MAX_VALUE) long duration,
      @NotNull TimeUnit timeUnit,
      @NotNull Clock clock) {
    this.supplier = supplier;
    this.cacheDuration = TimeUnit.MILLISECONDS.convert(duration, timeUnit);
    this.clock = clock;
  }

  /**
   * Returns the cached value, refreshing as required.
   *
   * @return the cached value
   */
  public T get() {
    long now = clock.millis();
    if (lastUpdate <= now - cacheDuration) {
      value = supplier.get();
      lastUpdate = now;
    }
    return value;
  }

}
