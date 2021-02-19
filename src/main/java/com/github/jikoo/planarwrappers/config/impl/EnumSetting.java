package com.github.jikoo.planarwrappers.config.impl;

import com.github.jikoo.planarwrappers.config.ParsedSimpleSetting;
import com.github.jikoo.planarwrappers.util.StringConverters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A setting representing an enum value.
 *
 * @param <T> the type of enum
 */
public class EnumSetting<T extends Enum<T>> extends ParsedSimpleSetting<T> {

  public EnumSetting(
      @NotNull ConfigurationSection section, @NotNull String key, @NotNull T defaultValue) {
    super(section, key, defaultValue);
  }

  @Override
  protected @Nullable T convertString(@Nullable String value) {
    return StringConverters.toEnum(defaultValue.getDeclaringClass(), value);
  }
}
