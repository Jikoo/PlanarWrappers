package com.github.jikoo.planarwrappers.config;

import java.util.function.Function;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A representation of a mapping available in a configuration.
 *
 * <p>Unlike a {@link Setting Setting&lt;java.util.Map&gt;}, a {@code Mapping} falls through to
 * default values even if there is an override for the world when the specified key does not result
 * in a value.
 *
 * @param <K> the type of key
 * @param <V> the type of value
 */
public abstract class Mapping<K, V> {

  protected final ConfigurationSection section;
  protected final String path;
  protected final Function<@NotNull K, @NotNull V> defaultMappings;
  private final String overridePath;

  /**
   * Constructor for a new Mapping.
   *
   * @param section the ConfigurationSection containing the Mapping
   * @param path the path of the Mapping
   * @param defaultMappings the function providing default mappings
   */
  protected Mapping(
      @NotNull ConfigurationSection section,
      @NotNull String path,
      @NotNull Function<@NotNull K, @NotNull V> defaultMappings) {
    Setting.checkValidPath(path);
    this.section = section;
    this.path = path;
    this.overridePath = Setting.getOverrideFormat(this.section, this.path);
    this.defaultMappings = defaultMappings;
  }

  /**
   * Get an overridable mapping for a key.
   *
   * <p>If an override is set, that takes precedence. If the override is not set, the default
   * mapping is used. If the default mapping also does not contain the specified key, the default
   * function is used.
   *
   * @param override the override
   * @param key the key to get a value for
   * @return the mapped value for the key
   */
  public @NotNull V get(@NotNull String override, @NotNull K key) {
    // Get value from override.
    V value = getPathSetting(String.format(overridePath, override), key);
    if (value != null) {
      return value;
    }

    // Fall through to base setting.
    value = getPathSetting(path, key);

    // Fall through to default if necessary.
    return value != null ? value : defaultMappings.apply(key);
  }

  /**
   * Get the value of a mapping for a specific path
   *
   * @param path the path
   * @param key the key of the mapping
   * @return the value or {@code null} if not present
   */
  protected abstract @Nullable V getPathSetting(@NotNull String path, @NotNull K key);
}
