package com.github.jikoo.planarwrappers.config;

import com.github.jikoo.planarwrappers.util.StringConverters;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.bukkit.Keyed;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class KeyedSetSetting<T extends Keyed> extends SimpleSetSetting<T> {

  protected KeyedSetSetting(
      @NotNull ConfigurationSection section, @NotNull String key, @NotNull Set<T> defaultValue) {
    super(section, key, defaultValue);
  }

  @Override
  protected @Nullable Set<T> convert(@NotNull String path) {
    List<String> values = section.getStringList(path);
    Set<T> convertedSet =
        StringConverters.toKeyedSet(values, this::convertValue, getTagRegistries(), getTagClass());
    return Collections.unmodifiableSet(convertedSet);
  }

  /**
   * Get the {@link Class} expected to be contained by a {@link org.bukkit.Tag Tag}.
   *
   * @return the Class
   */
  protected abstract @NotNull Class<T> getTagClass();

  /**
   * Get the registries in which a Tag may exist.
   *
   * @return the registry names
   */
  protected abstract @NotNull Collection<String> getTagRegistries();

  /**
   * Convert a {@link String} into a usable value.
   *
   * @param value the value in {@code String} form
   * @return the value or {@code null} if the value cannot be parsed
   */
  protected abstract @Nullable T convertValue(@NotNull String value);
}
