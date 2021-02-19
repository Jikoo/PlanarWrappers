package com.github.jikoo.planarwrappers.config.impl;

import com.github.jikoo.planarwrappers.config.ConfigSetting;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/** A setting representing an integer value. */
public class IntSetting extends ConfigSetting<Integer> {

  public IntSetting(
      @NotNull ConfigurationSection section, @NotNull String key, @NotNull Integer defaultValue) {
    super(section, key, ConfigurationSection::isInt, ConfigurationSection::getInt, defaultValue);
  }
}
