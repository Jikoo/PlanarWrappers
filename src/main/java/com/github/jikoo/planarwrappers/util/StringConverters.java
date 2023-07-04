package com.github.jikoo.planarwrappers.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility for converting from a String into another usable data type. Useful for loading
 * non-serializable values (like enum constants) from configurations.
 */
public final class StringConverters {

  private StringConverters() {
    throw new IllegalStateException("Cannot instantiate static utility classes!");
  }

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

    return NamespacedKey.fromString(key.toLowerCase(Locale.ROOT));
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

  public static @NotNull Set<Material> toMaterialSet(@NotNull Collection<String> values) {
    return toKeyedSet(
        values,
        StringConverters::toMaterial,
        List.of(Tag.REGISTRY_BLOCKS, Tag.REGISTRY_ITEMS),
        Material.class);
  }

  /**
   * Convert a {@link Collection} of {@link String Strings} into a {@link Set} of {@link Keyed}
   * objects. Supports {@link Tag Tags} using Mojang's convention - tags are declared using a hash.
   *
   * <p>For example, rather than declaring every fence gate (i.e. {@code [
   * minecraft:dark_oak_fence_gate, minecraft:spruce_fence_gate, ...]}) one can simply use the tag:
   * {@code [ #minecraft:fence_gates ]}.
   *
   * @param values the values to convert
   * @param converter the conversion function
   * @param registries the names of registries that may contain the tag
   * @param clazz the class of tag entries
   * @param <T> the type of Keyed
   * @return the converted values
   */
  public static <T extends Keyed> @NotNull Set<T> toKeyedSet(
      @NotNull Collection<String> values,
      @NotNull Function<String, T> converter,
      @NotNull Collection<String> registries,
      @NotNull Class<T> clazz) {
    Set<T> convertedSet = new HashSet<>();

    for (String value : values) {
      if (value.length() > 0 && value.charAt(0) == '#') {
        // Mojang's tag declaration convention is to start with #, i.e. #minecraft:fence_gates
        addTag(convertedSet, value, registries, clazz);
      } else {
        // Otherwise parse as usual.
        T converted = converter.apply(value);
        if (converted != null) {
          convertedSet.add(converted);
        }
      }
    }
    return convertedSet;
  }

  /**
   * Helper method for adding the contents of a {@link Tag} to an existing {@link Set}.
   *
   * @param set the set to add to
   * @param tagString the String representation of the Tag
   * @param registries the registries that may contain a Tag
   * @param clazz the Class of the Tag's content
   * @param <T> the type of Keyed
   */
  private static <T extends Keyed> void addTag(
      @NotNull Set<T> set,
      @NotNull String tagString,
      @NotNull Collection<String> registries,
      @NotNull Class<T> clazz) {
    // Tags have preceding identifier.
    String namespacedString = tagString.substring(1);
    NamespacedKey namespacedKey = StringConverters.toNamespacedKey(namespacedString);

    if (namespacedKey == null) {
      return;
    }

    for (String registry : registries) {
      Tag<T> tag = Bukkit.getTag(registry, namespacedKey, clazz);
      if (tag != null) {
        set.addAll(tag.getValues());
      }
    }
  }
}
