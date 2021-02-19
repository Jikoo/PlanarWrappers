package com.github.jikoo.planarwrappers.config.impl;

import com.github.jikoo.planarwrappers.config.ConfigSetting;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/** A setting representing a string value. */
public class StringSetting extends ConfigSetting<String> {

  public StringSetting(
      @NotNull ConfigurationSection section, @NotNull String key, @NotNull String defaultValue) {
    super(
        section,
        key,
        ConfigurationSection::isString,
        ConfigurationSection::getString,
        defaultValue);
  }
}
