package com.github.jikoo.planarwrappers.config.impl;

import com.github.jikoo.planarwrappers.config.ConfigSetting;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/** A setting representing a double value. */
public class DoubleSetting extends ConfigSetting<Double> {

  public DoubleSetting(
      @NotNull ConfigurationSection section, @NotNull String key, @NotNull Double defaultValue) {
    super(
        section,
        key,
        ConfigurationSection::isDouble,
        ConfigurationSection::getDouble,
        defaultValue);
  }
}
