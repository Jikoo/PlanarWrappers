package com.github.jikoo.planarwrappers.tuple;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Feature: Supplier that caches the returned value")
@TestInstance(Lifecycle.PER_METHOD)
class CachingSupplierTest {

  @DisplayName("Ensure that values are obtained as expected.")
  @ParameterizedTest
  @MethodSource("getBooleans")
  void testGet(Boolean value) {
    Supplier<Boolean> cache = new CachingSupplier<>(() -> value, 5, TimeUnit.MILLISECONDS);
    assertThat("Value must be supplied as expected", cache.get(), is(value));
  }

  @DisplayName("Ensure that values are cached.")
  @Test
  void testCachedValueUsed() {
    Supplier<Boolean> singleUseSupplier = new SingleUseSupplier<>(true);
    Supplier<Boolean> cache = new CachingSupplier<>(singleUseSupplier, 5, TimeUnit.MILLISECONDS);

    assertThat("Value must be supplied as expected", cache.get());
    assertThrows(
        IllegalStateException.class,
        singleUseSupplier::get,
        "Supplier may only be used once");
    assertDoesNotThrow(cache::get, "Supplier must only be used once");
  }

  @DisplayName("Ensure that values are re-obtained after the cache period expires.")
  @Test
  void testCachedValueExpires() {
    Supplier<Boolean> singleUseSupplier = new SingleUseSupplier<>(true);
    Clock clock = mock(Clock.class);
    when(clock.millis()).thenReturn(0L);
    Supplier<Boolean> cache = new CachingSupplier<>(
        singleUseSupplier,
        5,
        TimeUnit.MILLISECONDS,
        clock);
    assertThat("Value must be supplied as expected", cache.get());
    assertThrows(
        IllegalStateException.class,
        singleUseSupplier::get,
        "Supplier may only be used once");
    assertDoesNotThrow(cache::get, "Supplier must only be used once");

    when(clock.millis()).thenReturn(5L);
    assertThrows(
        IllegalStateException.class,
        cache::get,
        "Cache must expire after appropriate time period");
  }

  @Contract(pure = true)
  private static @NotNull Collection<Boolean> getBooleans() {
    return Arrays.asList(
        Boolean.TRUE,
        Boolean.FALSE,
        null
    );
  }

  private static class SingleUseSupplier<T> implements Supplier<T> {
    private final T value;
    private boolean used = false;

    private SingleUseSupplier(T value) {
      this.value = value;
    }

    @Override
    public T get() {
      if (used) {
        throw new IllegalStateException("Cannot be called more than once!");
      }
      used = true;
      return value;
    }

  }

}
