package com.github.jikoo.planarwrappers.config.impl;

import com.github.jikoo.planarwrappers.config.ConfigSetting;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/** A setting representing an {@link ItemStack}. */
public class ItemSetting extends ConfigSetting<ItemStack> {

  public ItemSetting(
      @NotNull ConfigurationSection section, @NotNull String key, @NotNull ItemStack defaultValue) {
    super(
        section,
        key,
        ConfigurationSection::isItemStack,
        ConfigurationSection::getItemStack,
        defaultValue);
  }
}
