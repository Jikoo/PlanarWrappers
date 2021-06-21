package com.github.jikoo.planarwrappers.config;

import com.github.jikoo.planarwrappers.util.StringConverters;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.bukkit.Keyed;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A setting for {@link Set Sets} of {@link Keyed} objects.
 *
 * @param <T> the type of object in the {@code Set}
 */
public abstract class KeyedSetSetting<T extends Keyed> extends SimpleSetSetting<T> {

  protected KeyedSetSetting(
      @NotNull ConfigurationSection section, @NotNull String key, @NotNull Set<T> defaultValue) {
    super(section, key, defaultValue);
  }

  @Override
  protected @Nullable Set<T> convert(@NotNull String path) {
    List<String> values = section.getStringList(path);
    Set<T> convertedSet =
        StringConverters.toKeyedSet(values, this::convertValue, getTagRegistries(), getTagClass());
    return Collections.unmodifiableSet(convertedSet);
  }

  /**
   * Get the {@link Class} expected to be contained by a {@link org.bukkit.Tag Tag}.
   *
   * @return the {@code Class}
   */
  protected abstract @NotNull Class<T> getTagClass();

  /**
   * Get the registries in which a {@link org.bukkit.Tag Tag} may exist.
   *
   * @return the registry names
   */
  protected abstract @NotNull Collection<String> getTagRegistries();
}
