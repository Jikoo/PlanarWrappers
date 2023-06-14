package com.github.jikoo.planarwrappers.util;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A utility for useful methods involving generics.
 *
 * @author Jikoo
 */
public final class Generics {

  private Generics() {
    throw new IllegalStateException("Cannot instantiate static utility classes!");
  }

  public static <T> @NotNull T orDefault(@Nullable T t, @NotNull T defaultT) {
    return t == null ? defaultT : t;
  }

  public static <T, R> @Nullable R functionAs(
      @NotNull Class<T> clazz, @Nullable Object obj, @NotNull Function<T, R> function) {
    if (!clazz.isInstance(obj)) {
      return null;
    }
    return function.apply(clazz.cast(obj));
  }

  public static <T> boolean consumeAs(
      @NotNull Class<T> clazz, @Nullable Object obj, @NotNull Consumer<T> consumer) {
    if (!clazz.isInstance(obj)) {
      return false;
    }
    consumer.accept(clazz.cast(obj));
    return true;
  }

  public static <T> boolean unaryBiConsumeAs(
      @NotNull Class<T> clazz,
      @Nullable Object obj1,
      @Nullable Object obj2,
      @NotNull BiConsumer<T, T> consumer) {
    if (!clazz.isInstance(obj1) || !clazz.isInstance(obj2)) {
      return false;
    }
    consumer.accept(clazz.cast(obj1), clazz.cast(obj2));
    return true;
  }
}
