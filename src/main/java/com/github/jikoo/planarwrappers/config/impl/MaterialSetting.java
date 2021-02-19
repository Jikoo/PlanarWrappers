package com.github.jikoo.planarwrappers.config.impl;

import com.github.jikoo.planarwrappers.config.ParsedSetting;
import com.github.jikoo.planarwrappers.util.StringConverters;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** A setting representing a {@link Material} value. */
public class MaterialSetting extends ParsedSetting<Material> {

  public MaterialSetting(
      @NotNull ConfigurationSection section, @NotNull String key, @NotNull Material defaultValue) {
    super(section, key, defaultValue);
  }

  @Override
  protected boolean test(@NotNull String path) {
    return section.isString(path);
  }

  @Nullable
  @Override
  protected Material convert(@NotNull String path) {
    return StringConverters.toMaterial(section.getString(path));
  }
}
