package com.github.jikoo.planarwrappers.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A ParsedSetting that fetches its value from a single string.
 *
 * @param <T>
 */
public abstract class ParsedSimpleSetting<T> extends ParsedSetting<T> {

  protected ParsedSimpleSetting(
      @NotNull ConfigurationSection section, @NotNull String path, @NotNull T defaultValue) {
    super(section, path, defaultValue);
  }

  @Override
  protected boolean test(@NotNull String path) {
    return section.isString(path);
  }

  @Override
  protected @Nullable T convert(@NotNull String path) {
    return convertString(section.getString(path));
  }

  /**
   * Convert a {@link String} into a value.
   *
   * @param value the {@code String}
   * @return the value {@code null} if the value cannot be parsed
   */
  protected abstract @Nullable T convertString(@Nullable String value);
}
