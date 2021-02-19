package com.github.jikoo.planarwrappers.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A more complex world setting that parses a value from an entire {@link ConfigurationSection}.
 *
 * @param <T> the type of value stored
 */
public abstract class ParsedComplexSetting<T> extends ParsedSetting<T> {

  protected ParsedComplexSetting(
      @NotNull ConfigurationSection section, @NotNull String path, @NotNull T defaultValue) {
    super(section, path, defaultValue);
  }

  @Override
  protected boolean test(@NotNull String path) {
    return section.contains(path);
  }

  @Override
  protected @Nullable T convert(@NotNull String path) {
    return convert(section.getConfigurationSection(path));
  }

  /**
   * Convert a {@link ConfigurationSection} into a usable value.
   *
   * @param value the {@code ConfigurationSection} representing the value
   * @return the value or {@code null} if the value cannot be parsed
   */
  protected abstract @Nullable T convert(@Nullable ConfigurationSection value);
}
