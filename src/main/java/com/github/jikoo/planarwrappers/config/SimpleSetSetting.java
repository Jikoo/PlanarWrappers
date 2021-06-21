package com.github.jikoo.planarwrappers.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A parsed setting representing a {@link Set} of a simple objects.
 *
 * <p>The "set" configuration-side is a list of strings.
 *
 * @param <T> the type of object in the {@code Set}
 */
public abstract class SimpleSetSetting<T> extends ParsedSetting<Set<T>> {

  protected SimpleSetSetting(
      @NotNull ConfigurationSection section, @NotNull String key, @NotNull Set<T> defaultValue) {
    super(section, key, Collections.unmodifiableSet(defaultValue));
  }

  @Override
  protected boolean test(@NotNull String path) {
    return section.isList(path);
  }

  @Override
  protected @Nullable Set<T> convert(@NotNull String path) {
    HashSet<T> convertedSet = new HashSet<>();
    List<String> values = section.getStringList(path);
    for (String value : values) {
      T converted = convertValue(value);
      if (converted != null) {
        convertedSet.add(converted);
      }
    }
    return Collections.unmodifiableSet(convertedSet);
  }

  /**
   * Convert a {@link String} into a usable value.
   *
   * @param value the value in {@code String} form
   * @return the value or {@code null} if the value cannot be parsed
   */
  protected abstract @Nullable T convertValue(@NotNull String value);
}
