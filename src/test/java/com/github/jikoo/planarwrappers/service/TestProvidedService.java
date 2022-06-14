package com.github.jikoo.planarwrappers.service;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestProvidedService<T> extends ProvidedService<T> {

  private @Nullable T value;

  protected TestProvidedService(@NotNull Plugin plugin) {
    this(plugin, null);
  }

  protected TestProvidedService(@NotNull Plugin plugin, @Nullable T initialValue) {
    super(plugin);
    this.value = initialValue;
  }

  public void setValue(@Nullable T value) {
    this.value = value;
    wrapClass(true);
  }

  @Override
  protected @Nullable T getRegistration(@NotNull Class<T> clazz) {
    return value;
  }

}
