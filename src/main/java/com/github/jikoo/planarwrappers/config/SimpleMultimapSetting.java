package com.github.jikoo.planarwrappers.config;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link Multimap} representation of a {@link ConfigurationSection} containing multiple lists of
 * strings.
 *
 * @param <K> the type of key used by the {@code Multimap}
 * @param <V> the type of value stored in the {@code Multimap}
 */
public abstract class SimpleMultimapSetting<K, V> extends ParsedComplexSetting<Multimap<K, V>> {

  protected SimpleMultimapSetting(
      @NotNull ConfigurationSection section,
      @NotNull String path,
      @NotNull Multimap<K, V> defaultValue) {
    super(section, path, ImmutableMultimap.copyOf(defaultValue));
  }

  @Override
  protected @Nullable Multimap<K, V> convert(@Nullable ConfigurationSection value) {
    Multimap<K, V> multimap = HashMultimap.create();

    // Section is set, just isn't a parseable ConfigurationSection.
    if (value == null) {
      return multimap;
    }

    for (String section1Key : value.getKeys(true)) {
      K convertedKey = convertKey(section1Key);
      if (convertedKey == null || !value.isList(section1Key)) {
        continue;
      }

      for (String rawValue : value.getStringList(section1Key)) {
        V convertedValue = convertValue(rawValue);
        if (convertedValue != null) {
          multimap.put(convertedKey, convertedValue);
        }
      }
    }

    return ImmutableMultimap.copyOf(multimap);
  }

  /**
   * Convert a {@link String} into a usable key.
   *
   * @param key the key in {@code String} form
   * @return the key or {@code null} if the key cannot be parsed
   */
  protected abstract @Nullable K convertKey(@NotNull String key);

  /**
   * Convert a {@link String} into a usable value.
   *
   * @param value the value in {@code String} form
   * @return the value or {@code null} if the value cannot be parsed
   */
  protected abstract @Nullable V convertValue(@NotNull String value);
}
