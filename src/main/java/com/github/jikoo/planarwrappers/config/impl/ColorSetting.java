package com.github.jikoo.planarwrappers.config.impl;

import com.github.jikoo.planarwrappers.config.ConfigSetting;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/** A setting representing a color value. */
public class ColorSetting extends ConfigSetting<Color> {

  public ColorSetting(
      @NotNull ConfigurationSection section, @NotNull String key, @NotNull Color defaultValue) {
    super(
        section, key, ConfigurationSection::isColor, ConfigurationSection::getColor, defaultValue);
  }
}
