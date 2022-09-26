package com.github.jikoo.planarwrappers.scheduler;

import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

/**
 * A container for utility methods for converting {@link TimeUnit TimeUnits} to and from Minecraft
 * ticks (50 milliseconds).
 */
public enum TickTimeUnit {

  ;

  private static final long MILLIS_PER_TICK = 50;

  /**
   * Convert from the specified {@link TimeUnit} to Minecraft ticks. As with
   * {@link TimeUnit#convert(long, TimeUnit)}, less granular values are truncated. For example, 49
   * milliseconds is converted to 0 ticks.
   *
   * @param sourceDuration the duration in source units
   * @param sourceUnit the source unit
   * @return the number of units of converted time
   */
  public static long toTicks(long sourceDuration, @NotNull TimeUnit sourceUnit) {
    return TimeUnit.MILLISECONDS.convert(sourceDuration, sourceUnit) / MILLIS_PER_TICK;
  }

  /**
   * Convert from Minecraft ticks to the specified {@link TimeUnit}. As with
   * {@link TimeUnit#convert(long, TimeUnit)}, less granular values are truncated. For example, 19
   * ticks is converted to 0 seconds.
   *
   * @param ticks the number of ticks
   * @param destinationUnit the unit of time
   * @return the number of units of converted time
   */
  public static long toTime(long ticks, @NotNull TimeUnit destinationUnit) {
    long millis = ticks * MILLIS_PER_TICK;
    if (destinationUnit == TimeUnit.MILLISECONDS) {
      // Don't bother converting milliseconds to milliseconds.
      return millis;
    }

    return destinationUnit.convert(millis, TimeUnit.MILLISECONDS);
  }

}
