package com.github.jikoo.planarwrappers.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Function;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MappingTest {

  private static final String NULL_KEY = "null";
  private static final String INVALID_VALUE_KEY = "totally_invalid_value";

  @DisplayName("Mappings should be retrievable as expected")
  @Test
  void testMapping() {
    YamlConfiguration configuration = new YamlConfiguration();

    String path = "mapped_value";
    String override = "special_place";
    String overridePath =
        String.format(Setting.getOverrideFormat(configuration, path), override) + '.';

    String key = "key";
    String valueDefault = "please ignore";
    configuration.set(path + '.' + key, valueDefault);

    String valueOverride = "please do not ignore";
    configuration.set(overridePath + key, valueOverride);

    String otherKey = "otherKey";
    String otherDefault = "sample text";
    configuration.set(path + '.' + otherKey, otherDefault);

    configuration.set(overridePath + NULL_KEY, "null key value");
    configuration.set(overridePath + INVALID_VALUE_KEY, "golly");

    Mapping<String, String> mapping = new TestMapping(configuration, path);

    assertThat("Override must be used", mapping.get(override, key), is(valueOverride));
    // Repeat to hit cache.
    assertThat("Override must be used", mapping.get(override, key), is(valueOverride));

    assertThat(
        "Override must fall through if invalid/absent",
        mapping.get("INVALID_OVERRIDE", key),
        is(valueDefault));
    assertThat(
        "Override must fall through if invalid/absent",
        mapping.get(override, otherKey),
        is(otherDefault));

    String fallthrough = "invalid_key";
    assertThat(
        "Override must fall through to default provider",
        mapping.get("INVALID_OVERRIDE", fallthrough),
        is(fallthrough));

    assertThat("Null key must fall through", mapping.get(override, NULL_KEY), is(NULL_KEY));

    assertThat(
        "Invalid value must fall through",
        mapping.get(override, INVALID_VALUE_KEY),
        is(INVALID_VALUE_KEY));
  }

  @DisplayName("Data holders should not allow invalid paths as a key.")
  @ParameterizedTest
  @ValueSource(strings = {"overrides", ""})
  void testBlockedKey(String key) {
    YamlConfiguration config = new YamlConfiguration();

    assertThrows(IllegalArgumentException.class, () -> new TestMapping(config, key));
  }

  private static class TestMapping extends ParsedMapping<String, String> {

    protected TestMapping(@NotNull ConfigurationSection section, @NotNull String path) {
      super(section, path, Function.identity());
    }

    @Override
    protected @Nullable String convertKey(@NotNull String key) {
      if (key.equals(NULL_KEY)) {
        return null;
      }
      return key;
    }

    @Override
    protected boolean testValue(@NotNull ConfigurationSection localSection, @NotNull String path) {
      if (path.contains(INVALID_VALUE_KEY)) {
        return false;
      }
      return localSection.isString(path);
    }

    @Override
    protected @Nullable String convertValue(
        @NotNull ConfigurationSection localSection, @NotNull String path) {
      return localSection.getString(path);
    }
  }
}
