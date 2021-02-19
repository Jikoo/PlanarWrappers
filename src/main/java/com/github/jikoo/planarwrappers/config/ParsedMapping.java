package com.github.jikoo.planarwrappers.config;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * A Mapping that is not directly retrievable from a {@link ConfigurationSection}.
 *
 * <p>To minimize the impact of the extra handling required, values are cached after being parsed.
 *
 * @param <K> the type of key
 * @param <V> the type of value
 */
public abstract class ParsedMapping<K, V> extends Mapping<K, V> {

  @VisibleForTesting final Map<String, Map<K, V>> cache = new HashMap<>();

  protected ParsedMapping(
      @NotNull ConfigurationSection section,
      @NotNull String path,
      @NotNull Function<@NotNull K, @NotNull V> defaultValue) {
    super(section, path, defaultValue);
  }

  @Override
  protected @Nullable V getPathSetting(@NotNull String path, @NotNull K key) {
    if (cache.containsKey(path)) {
      Map<K, V> cachedMap = cache.get(path);
      if (cachedMap != null && cachedMap.containsKey(key)) {
        return cachedMap.get(key);
      }
      return null;
    }

    if (!section.isConfigurationSection(path)) {
      cache.put(path, null);
      return null;
    }

    ConfigurationSection localSection = section.getConfigurationSection(path);
    assert localSection != null;

    Map<K, V> mappings = new HashMap<>();
    for (String rawKey : localSection.getKeys(true)) {
      K parsedKey = convertKey(rawKey);

      if (parsedKey == null || !testValue(localSection, rawKey)) {
        continue;
      }

      V parsedValue = convertValue(localSection, rawKey);
      if (parsedValue != null) {
        mappings.put(parsedKey, parsedValue);
      }
    }

    cache.put(path, mappings.isEmpty() ? null : mappings);

    return mappings.get(key);
  }

  /**
   * Convert a {@link String} into a usable key.
   *
   * @param key the {@code String} value of the key
   * @return the key or {@code null} if the key cannot be parsed
   */
  protected abstract @Nullable K convertKey(@NotNull String key);

  /**
   * Check if a {@link ConfigurationSection} has a usable value set at the given path.
   *
   * @param localSection the {@code ConfigurationSection}
   * @param path the path of the value
   * @return {@code true} if the value is usable
   */
  protected abstract boolean testValue(
      @NotNull ConfigurationSection localSection, @NotNull String path);

  /**
   * Convert a configuration value into a usable value.
   *
   * @param localSection the ConfigurationSection containing the value
   * @param path the path of the value
   * @return the value or {@code null} if the value cannot be parsed
   */
  protected abstract @Nullable V convertValue(
      @NotNull ConfigurationSection localSection, @NotNull String path);
}
