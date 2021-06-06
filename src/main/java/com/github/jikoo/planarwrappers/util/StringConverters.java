package com.github.jikoo.planarwrappers.util;

import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility for converting from a String into another usable data type. Useful for loading
 * non-serializable values (like enum constants) from configurations.
 */
public final class StringConverters {

  private static final Pattern VALID_NAMESPACE = Pattern.compile("([a-z0-9._-]+:)?[a-z0-9/._-]+");

  private StringConverters() {}

  /**
   * Convert a String into a NamespacedKey.
   *
   * @param key the namespaced key string
   * @return the parsed NamespacedKey or null if invalid
   */
  @Contract("null -> null")
  public static @Nullable NamespacedKey toNamespacedKey(@Nullable String key) {
    if (key == null) {
      return null;
    }

    key = key.toLowerCase(Locale.ROOT);

    if (!VALID_NAMESPACE.matcher(key).matches()) {
      return null;
    }

    NamespacedKey namespacedKey;
    if (key.indexOf(':') < 0) {
      namespacedKey = NamespacedKey.minecraft(key);
    } else {
      String[] split = key.split(":");
      // No alternative to deprecated API.
      //noinspection deprecation
      namespacedKey = new NamespacedKey(split[0], split[1]);
    }

    return namespacedKey;
  }

  /**
   * Convert a String into a {@link Keyed} object.
   *
   * @param function the method for getting a Keyed object from a NamespacedKey
   * @param key the raw String value
   * @param <T> the type of Keyed
   * @return the value associated with the key
   */
  @Contract("_, null -> null")
  public static <T extends Keyed> @Nullable T toKeyed(
      @NotNull Function<NamespacedKey, T> function, @Nullable String key) {
    NamespacedKey namespacedKey = toNamespacedKey(key);

    if (namespacedKey == null) {
      return null;
    }

    return function.apply(namespacedKey);
  }

  /**
   * Convert a String into an {@link Enum} constant.
   *
   * @param clazz the Enum class
   * @param name the name of the Enum constant
   * @param <T> the type of Enum
   * @return the constant
   */
  @Contract("_, null -> null")
  public static <T extends Enum<T>> @Nullable T toEnum(
      @NotNull Class<T> clazz, @Nullable String name) {
    if (name == null) {
      return null;
    }

    try {
      return Enum.valueOf(clazz, name);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Convert a String into an {@link Enchantment}.
   *
   * @param key the key
   * @return the Enchantment
   * @see #toKeyed(Function, String)
   */
  @Contract("null -> null")
  public static @Nullable Enchantment toEnchant(@Nullable String key) {
    return toKeyed(Enchantment::getByKey, key);
  }

  /**
   * Convert a String into a {@link Material}.
   *
   * <p>Prioritizes namespaced key matching, but falls through to matched material names.
   *
   * @param key the key
   * @return the Material
   * @see #toKeyed(Function, String)
   */
  @Contract("null -> null")
  public static @Nullable Material toMaterial(@Nullable String key) {
    if (key == null) {
      return null;
    }

    Material value = toKeyed(Registry.MATERIAL::get, key);

    if (value != null) {
      return value;
    }

    return Material.matchMaterial(key);
  }
}
