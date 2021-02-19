package com.github.jikoo.planarwrappers.config.impl;

import com.github.jikoo.planarwrappers.config.ConfigSetting;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/** A setting representing a {@link Vector}. */
public class VectorSetting extends ConfigSetting<Vector> {

  public VectorSetting(
      @NotNull ConfigurationSection section, @NotNull String key, @NotNull Vector defaultValue) {
    super(
        section,
        key,
        ConfigurationSection::isVector,
        ConfigurationSection::getVector,
        defaultValue);
  }
}
