package com.github.jikoo.planarwrappers.config.impl;

import com.github.jikoo.planarwrappers.config.ConfigSetting;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/** A setting representing a {@link Location}. */
public class LocationSetting extends ConfigSetting<Location> {

  public LocationSetting(
      @NotNull ConfigurationSection section, @NotNull String key, @NotNull Location defaultValue) {
    super(
        section,
        key,
        ConfigurationSection::isLocation,
        ConfigurationSection::getLocation,
        defaultValue);
  }
}
