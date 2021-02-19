package com.github.jikoo.planarwrappers.config.impl;

import com.github.jikoo.planarwrappers.config.ConfigSetting;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/** A setting representing a long value. */
public class LongSetting extends ConfigSetting<Long> {

  public LongSetting(
      @NotNull ConfigurationSection section, @NotNull String key, @NotNull Long defaultValue) {
    super(section, key, ConfigurationSection::isLong, ConfigurationSection::getLong, defaultValue);
  }
}
