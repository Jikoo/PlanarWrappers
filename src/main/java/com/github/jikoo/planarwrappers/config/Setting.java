package com.github.jikoo.planarwrappers.config;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A generic framework for handling a setting that may be configured per-world.
 *
 * @param <T> the type of value stored
 */
public abstract class Setting<T> {

  private static final String OVERRIDE_PATH_BASE = "overrides";

  protected final ConfigurationSection section;
  protected final String path;
  protected final T defaultValue;
  private final String overridesFormat;

  /**
   * Constructor for a new Setting.
   *
   * @param section the ConfigurationSection containing the Setting
   * @param path the path of the Setting
   * @param defaultValue the default value
   */
  protected Setting(
      @NotNull ConfigurationSection section, @NotNull String path, @NotNull T defaultValue) {
    checkValidPath(path);
    this.section = section;
    this.path = path;
    this.overridesFormat = getOverrideFormat(this.section, this.path);
    this.defaultValue = defaultValue;
  }

  static String getOverrideFormat(ConfigurationSection section, String path) {
    Configuration root = section.getRoot();
    // Support modified separator character.
    char separator = root == null ? '.' : root.options().pathSeparator();
    // Because this is a format string, % must be escaped.
    path = path.replace("%", "%%");
    return OVERRIDE_PATH_BASE + separator + "%s" + separator + path;
  }

  static void checkValidPath(String path) {
    if (path.isEmpty()) {
      throw new IllegalArgumentException("Path may not be blank!");
    }
    if (OVERRIDE_PATH_BASE.equals(path)) {
      throw new IllegalArgumentException("Key is reserved for setting overrides!");
    }
  }

  /**
   * Get an overridable value.
   *
   * <p>If an override is set, that takes precedence. If the override is not set, the default
   * setting is used. If the default setting also does not contain a usable value, the internal
   * default is used.
   *
   * @param override the override
   * @return the value
   */
  public @NotNull T get(@NotNull String override) {
    // Get value from override.
    T t = getPathSetting(String.format(overridesFormat, override));

    if (t != null) {
      return t;
    }

    // Fall through to base setting.
    t = getPathSetting(path);

    // Fall through to default if necessary.
    return t != null ? t : defaultValue;
  }

  /**
   * Get the value of a setting for a specific path.
   *
   * @param path the path
   * @return the value or {@code null} if not present
   */
  protected abstract @Nullable T getPathSetting(@NotNull String path);
}
