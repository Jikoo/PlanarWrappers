package com.github.jikoo.planarwrappers.lang;

import org.jetbrains.annotations.NotNull;

/**
 * A data holder for a translation location and default value.
 *
 * @param key the configuration location where the value is stored
 * @param defaultValue the default value for the message, if unset
 */
public record Message(@NotNull String key, @NotNull String defaultValue) implements Replacement {

  @Override
  public String getPlaceholder() {
    return '{' + key + '}';
  }

  @Override
  public String getValue() {
    return defaultValue;
  }

}
