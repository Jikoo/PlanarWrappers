package com.github.jikoo.planarwrappers.config.impl;

import com.github.jikoo.planarwrappers.config.KeyedSetSetting;
import com.github.jikoo.planarwrappers.util.StringConverters;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** A setting representing a {@link Set} of {@link Material Materials}. */
public class MaterialSetSetting extends KeyedSetSetting<Material> {

  public MaterialSetSetting(
      @NotNull ConfigurationSection section,
      @NotNull String key,
      @NotNull Set<Material> defaultValue) {
    super(section, key, defaultValue);
  }

  @Override
  protected @NotNull Class<Material> getTagClass() {
    return Material.class;
  }

  @Override
  protected @NotNull Collection<String> getTagRegistries() {
    return Arrays.asList("items", "blocks");
  }

  @Override
  protected @Nullable Material convertValue(@NotNull String value) {
    return StringConverters.toMaterial(value);
  }
}
