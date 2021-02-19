package com.github.jikoo.planarwrappers.config;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * A setting that is not directly retrievable from a {@link ConfigurationSection}.
 *
 * <p>To minimize the impact of the extra handling required, values are cached after being parsed.
 *
 * @param <T> the type of value stored
 */
public abstract class ParsedSetting<T> extends Setting<T> {

  @VisibleForTesting final Map<String, T> cache = new HashMap<>();

  protected ParsedSetting(
      @NotNull ConfigurationSection section, @NotNull String path, @NotNull T defaultValue) {
    super(section, path, defaultValue);
  }

  @Override
  protected @NotNull T getPathSetting(@NotNull String path) {
    if (cache.containsKey(path)) {
      return cache.get(path);
    }

    T value = null;
    if (test(path)) {
      value = convert(path);
    }

    if (value == null && test(this.path)) {
      value = convert(this.path);
    }

    if (value == null) {
      value = defaultValue;
    }

    cache.put(path, value);
    return value;
  }

  /**
   * Check if the given path has a usable value.
   *
   * @param path the path of the value
   * @return {@code true} if the value is usable
   */
  protected abstract boolean test(@NotNull String path);

  /**
   * Convert a path into a usable value.
   *
   * @param path the path of the value
   * @return the value or {@code null} if the value cannot be parsed
   */
  protected abstract @Nullable T convert(@NotNull String path);
}
