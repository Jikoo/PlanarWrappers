package com.github.jikoo.planarwrappers.config;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A generic world setting that is retrievable directly from a ConfigurationSection.
 *
 * @param <T> the type of value stored
 */
public abstract class ConfigSetting<T> extends Setting<T> {

  private final BiPredicate<@NotNull ConfigurationSection, @NotNull String> tester;
  private final BiFunction<@NotNull ConfigurationSection, @NotNull String, T> getter;

  /**
   * Constructor for a ConfigSetting.
   *
   * @param section the ConfigurationSection containing the Setting
   * @param path the path of the Setting
   * @param tester the ConfigurationSection method for checking for a valid value
   * @param getter the ConfigurationSection method for obtaining a value
   * @param defaultValue the default value
   */
  protected ConfigSetting(
      @NotNull ConfigurationSection section,
      @NotNull String path,
      @NotNull BiPredicate<@NotNull ConfigurationSection, @NotNull String> tester,
      @NotNull BiFunction<@NotNull ConfigurationSection, @NotNull String, T> getter,
      @NotNull T defaultValue) {
    super(section, path, defaultValue);
    this.tester = tester;
    this.getter = getter;
  }

  protected @Nullable T getPathSetting(@NotNull String path) {
    if (tester.test(section, path)) {
      return getter.apply(section, path);
    }

    return null;
  }
}
