package com.github.jikoo.planarwrappers.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.jikoo.planarwrappers.config.impl.BooleanSetting;
import com.github.jikoo.planarwrappers.config.impl.ColorSetting;
import com.github.jikoo.planarwrappers.config.impl.DoubleSetting;
import com.github.jikoo.planarwrappers.config.impl.EnumSetting;
import com.github.jikoo.planarwrappers.config.impl.IntSetting;
import com.github.jikoo.planarwrappers.config.impl.ItemSetting;
import com.github.jikoo.planarwrappers.config.impl.LocationSetting;
import com.github.jikoo.planarwrappers.config.impl.LongSetting;
import com.github.jikoo.planarwrappers.config.impl.MaterialSetSetting;
import com.github.jikoo.planarwrappers.config.impl.MaterialSetting;
import com.github.jikoo.planarwrappers.config.impl.StringSetting;
import com.github.jikoo.planarwrappers.config.impl.VectorSetting;
import com.github.jikoo.planarwrappers.function.TriFunction;
import com.github.jikoo.planarwrappers.util.StringConverters;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.Tag;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Feature: Parse and handle overridable settings.")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SettingTest {

  @BeforeAll
  void beforeAll() {
    Server mock = mock(Server.class);
    when(mock.getLogger()).thenReturn(Logger.getLogger("bukkit"));
    when(mock.getRegistry(notNull())).thenReturn(null);
    when(mock.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("wall_signs"), Material.class))
        .thenReturn(new Tag<>() {
          private final Set<Material> materials = Set.of(Material.CRIMSON_WALL_SIGN, Material.WARPED_WALL_SIGN);
          @Override
          public boolean isTagged(@NotNull Material item) {
            return materials.contains(item);
          }

          @NotNull
          @Override
          public Set<Material> getValues() {
            return materials;
          }

          @NotNull
          @Override
          public NamespacedKey getKey() {
            return NamespacedKey.minecraft("wall_signs");
          }
        });
    Bukkit.setServer(mock);
  }

  private static final String INVALID_OVERRIDE = "%invalid%";
  private static final String OVERRIDE_VALUE = "world";

  @DisplayName("Settings should be retrievable as expected")
  @ParameterizedTest
  @MethodSource("getSettings")
  void testSetting(Setting<?> setting, Object internalDefault, Object setDefault, Object override) {
    assertThat("Override must be used", setting.get(OVERRIDE_VALUE), is(override));
    // Repeat run to hit cache on parsed settings.
    assertThat("Override must be used", setting.get(OVERRIDE_VALUE), is(override));
    assertThat(
        "Override must fall through if invalid/absent",
        setting.get(INVALID_OVERRIDE),
        is(setDefault));
    // Clear values for default fallthrough.
    setting.section.set(setting.path, null);
    if (setting instanceof ParsedSetting) {
      ((ParsedSetting<?>) setting).cache.clear();
    }
    assertThat(
        "Default must fall through if invalid/absent",
        setting.get(INVALID_OVERRIDE),
        is(internalDefault));
  }

  static Stream<Arguments> getSettings() {
    return Arrays.stream(
        new Arguments[] {
          makeSetting(BooleanSetting::new, "boolean_value", false, true, false),
          makeSetting(ColorSetting::new, "color_value", Color.RED, Color.WHITE, Color.BLACK),
          makeSetting(DoubleSetting::new, "double_value", 1.5, 0., 10.),
          makeSetting(
              EnumSetting::new,
              "enum_value",
              EntityType.AREA_EFFECT_CLOUD,
              EntityType.ARMOR_STAND,
              EntityType.ARROW),
          makeSetting(IntSetting::new, "int_value", 2, 1, 5),
          makeSetting(
              ItemSetting::new,
              "item_value",
              new ItemStack(Material.ACACIA_BOAT),
              new ItemStack(Material.DIRT),
              new ItemStack(Material.DIAMOND)),
          makeSetting(
              LocationSetting::new,
              "location_value",
              new Location(null, 5, 5, 5),
              new Location(null, 0, 10, 0),
              new Location(null, 10, 50, -30)),
          makeSetting(LongSetting::new, "long_value", 20L, 500L, 40L),
          makeSetting(
              MaterialSetting::new,
              "material_value",
              Material.STONE_BRICKS,
              Material.AIR,
              Material.DIRT),
          makeSetting(
              StringSetting::new,
              "string_value",
              "internal default",
              "default value",
              "special value"),
          makeSetting(
              VectorSetting::new,
              "vector_value",
              new Vector(1, 1, 1),
              new Vector(10, 5, 1),
              new Vector(20, 10, 2)),
          makeSet(),
          makeMaterialSet(),
          makeMultimap()
        });
  }

  private static <T> Arguments makeSetting(
      TriFunction<ConfigurationSection, String, T, Setting<T>> constructor,
      String path,
      T internalDefault,
      T setDefault,
      T override) {
    Setting<T> setting =
        constructor.apply(getConfiguration(path, setDefault, override), path, internalDefault);
    return Arguments.of(setting, internalDefault, setDefault, override);
  }

  private static Arguments makeSet() {
    Set<Material> internalDefault = Collections.singleton(Material.BAMBOO);
    List<Material> setDefault = Arrays.asList(Material.AIR, Material.ACACIA_BOAT);
    List<Material> override = Arrays.asList(Material.WET_SPONGE, Material.ARROW);
    String path = "set_value";
    YamlConfiguration configuration =
        getConfiguration(
            path,
            setDefault.stream().map(Material::name).toList(),
            override.stream().map(Material::name).toList());
    SimpleSetSetting<Material> setting =
        new SimpleSetSetting<>(configuration, path, internalDefault) {
          @Override
          protected @Nullable Material convertValue(@NotNull String value) {
            return StringConverters.toMaterial(value);
          }
        };

    return Arguments.of(
        setting, internalDefault, new HashSet<>(setDefault), new HashSet<>(override));
  }

  private static Arguments makeMaterialSet() {
    Set<Material> internalDefault = Collections.singleton(Material.BAMBOO);
    List<Material> setDefault = new ArrayList<>(Arrays.asList(Material.AIR, Material.ACACIA_BOAT));
    List<Material> override = Arrays.asList(Material.WET_SPONGE, Material.ARROW);
    String path = "set_value";
    YamlConfiguration configuration =
        getConfiguration(
            path,
            Stream.concat(
                    setDefault.stream().map(Material::name),
                    Stream.of("#wall_signs", "#3badnamespace", "#not_a_real_tag"))
                .toList(),
            override.stream().map(Material::name).toList());
    SimpleSetSetting<Material> setting =
        new MaterialSetSetting(configuration, path, internalDefault);

    setDefault.addAll(Tag.WALL_SIGNS.getValues());

    return Arguments.of(
        setting, internalDefault, new HashSet<>(setDefault), new HashSet<>(override));
  }

  private static Arguments makeMultimap() {
    Multimap<String, Material> internalDefault = HashMultimap.create();
    internalDefault.putAll("cool stuff", Arrays.asList(Material.ACACIA_BOAT, Material.ACACIA_DOOR));
    internalDefault.put("less cool stuff", Material.ACACIA_SLAB);
    internalDefault = ImmutableMultimap.copyOf(internalDefault);

    Multimap<String, Material> setDefault = HashMultimap.create();
    setDefault.putAll("neat", Arrays.asList(Material.POTTED_RED_MUSHROOM, Material.AIR));
    setDefault.put("cool", Material.ACACIA_DOOR);
    setDefault = ImmutableMultimap.copyOf(setDefault);

    YamlConfiguration configuration = new YamlConfiguration();
    String path = "multimap_value";

    setMultimap(configuration, path, setDefault);

    Multimap<String, Material> override = HashMultimap.create();
    override.putAll("other neat value", Arrays.asList(Material.GOLD_ORE, Material.TERRACOTTA));
    override = ImmutableMultimap.copyOf(override);
    setMultimap(
        configuration,
        String.format(Setting.getOverrideFormat(configuration, path), OVERRIDE_VALUE),
        override);

    SimpleMultimapSetting<String, Material> setting =
        new SimpleMultimapSetting<>(configuration, path, internalDefault) {

          @Override
          protected @NotNull String convertKey(@NotNull String key) {
            return key;
          }

          @Override
          protected @Nullable Material convertValue(@NotNull String value) {
            return StringConverters.toMaterial(value);
          }
        };

    return Arguments.of(setting, internalDefault, setDefault, override);
  }

  private static Object translate(Object object) {
    if (object instanceof Enum) {
      return ((Enum<?>) object).name();
    }
    if (object instanceof Keyed) {
      return ((Keyed) object).getKey().toString();
    }
    return object;
  }

  private static YamlConfiguration getConfiguration(String path, Object value, Object override) {
    YamlConfiguration configuration = new YamlConfiguration();
    value = translate(value);
    override = translate(override);
    configuration.set(path, value);
    configuration.set(
        String.format(Setting.getOverrideFormat(configuration, path), OVERRIDE_VALUE), override);
    return configuration;
  }

  private static <T> void setMultimap(
      ConfigurationSection section, String path, Multimap<String, T> mappings) {
    ConfigurationSection localSection = section.createSection(path);
    for (Entry<String, Collection<T>> mapping : mappings.asMap().entrySet()) {
      localSection.set(
          mapping.getKey(),
          mapping.getValue().stream()
              .filter(Objects::nonNull)
              .map(SettingTest::translate)
              .toList());
    }
  }

  @DisplayName("Multimap settings should ignore invalid invalid configurations gracefully")
  @ParameterizedTest
  @MethodSource("getInvalidMultimapConfigs")
  void testInvalidMultimapInput(String key, Object value) {
    YamlConfiguration configuration = new YamlConfiguration();
    configuration.set(key, value);
    SimpleMultimapSetting<Material, String> setting =
        new SimpleMultimapSetting<>(
            configuration, "multimap_value", ImmutableMultimap.of()) {
          @Override
          protected @Nullable Material convertKey(@NotNull String key) {
            return StringConverters.toMaterial(key);
          }

          @Override
          protected @NotNull String convertValue(@NotNull String value) {
            return value;
          }
        };
    assertDoesNotThrow(() -> setting.get(INVALID_OVERRIDE));
  }

  private Stream<Arguments> getInvalidMultimapConfigs() {
    return Arrays.stream(
        new Arguments[] {
          Arguments.of("multimap_value.invalid_material", Arrays.asList("option 1", "option 2")),
          Arguments.of("multimap_value.AIR", 10L),
          Arguments.of("multimap_value", "A string is not a ConfigurationSection")
        });
  }

  @DisplayName("Data holders should not allow invalid paths as a key.")
  @ParameterizedTest
  @ValueSource(strings = {"overrides", ""})
  void testBlockedKey(String key) {
    YamlConfiguration config = new YamlConfiguration();

    assertThrows(
        IllegalArgumentException.class, () -> new EnumSetting<>(config, key, Material.AIR));
  }
}
