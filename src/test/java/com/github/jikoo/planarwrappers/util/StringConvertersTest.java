package com.github.jikoo.planarwrappers.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doReturn;

import com.github.jikoo.planarwrappers.mock.ServerMocks;
import com.github.jikoo.planarwrappers.mock.inventory.EnchantmentMocks;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@DisplayName("Feature: String converters should provide values for strings.")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StringConvertersTest {

  @BeforeAll
  void beforeAll() {
    Server server = ServerMocks.newServer();
    Tag<Material> wallSigns = Tag.WALL_SIGNS;
    doReturn(Set.of(Material.CRIMSON_WALL_SIGN, Material.WARPED_WALL_SIGN)).when(wallSigns).getValues();
    doReturn(wallSigns).when(server).getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("wall_signs"), Material.class);

    EnchantmentMocks.init();
  }

  @Test
  void testEnumInvalid() {
    assertThat("Value must be null.", StringConverters.toEnum(Tests.class, null), nullValue());
    assertThat("Value must be null.", StringConverters.toEnum(Tests.class, "invalid"), nullValue());
  }

  @Test
  void testEnumPresent() {
    Tests tests = Tests.TWO;
    assertThat(
        "Value must be obtained correctly.",
        StringConverters.toEnum(Tests.class, tests.name()),
        is(tests));
  }

  @Test
  void testKeyedInvalid() {
    assertThat("Value must be null.", StringConverters.toEnchant(null), nullValue());
    assertThat(
        "Value must be null.", StringConverters.toEnchant("bad key:good_value"), nullValue());
    assertThat(
        "Value must be null.", StringConverters.toEnchant("good_key:bad value"), nullValue());
    assertThat("Value must be null.", StringConverters.toEnchant("bad value"), nullValue());
    assertThat(
        "Value must be null.", StringConverters.toEnchant("not_an_enchantment"), nullValue());
  }

  @Test
  void testKeyedPresent() {
    Enchantment enchantment = Enchantment.SILK_TOUCH;
    Enchantment fromNamespaced = StringConverters.toEnchant(enchantment.getKeyOrThrow().toString());
    assertThat("Value must not be null", fromNamespaced, notNullValue());
    assertThat(
        "Value must be obtained from namespaced key.",
        fromNamespaced.getKeyOrThrow(),
        is(enchantment.getKeyOrThrow()));
    Enchantment fromUnNamespaced = StringConverters.toEnchant(enchantment.getKeyOrThrow().getKey());
    assertThat("Value must not be null", fromUnNamespaced, notNullValue());
    assertThat(
        "Value must be obtained from un-namespaced key.",
        fromUnNamespaced.getKeyOrThrow(),
        is(enchantment.getKeyOrThrow()));
  }

  @Test
  void testToKeyedSet() {
    assertThat(
        "Values must be obtained from tags and keys",
        StringConverters.toMaterialSet(
            List.of("#wall_signs", "#$invalid", "#fake:tag", "gold_ore", "$invalid")),
        is(Set.of(Material.CRIMSON_WALL_SIGN, Material.WARPED_WALL_SIGN, Material.GOLD_ORE)));
  }

  @Test
  void testMaterialInvalid() {
    assertThat("Value must be null.", StringConverters.toMaterial(null), nullValue());
    assertThat("Value must be null.", StringConverters.toMaterial("invalid"), nullValue());
  }

  @Test
  void testMaterialPresent() {
    Material material = Material.GOLD_ORE;
    assertThat(
        "Value must be obtained from namespaced key.",
        StringConverters.toMaterial(material.getKeyOrThrow().toString()),
        is(material));
    assertThat(
        "Value must be obtained from un-namespaced key.",
        StringConverters.toMaterial(material.getKeyOrThrow().getKey()),
        is(material));
    assertThat(
        "Value must be obtained from raw name.",
        StringConverters.toMaterial(material.name()),
        is(material));
    assertThat(
        "Value must be obtained from friendly name.",
        StringConverters.toMaterial(material.name().replace('_', ' ')),
        is(material));
  }

  private enum Tests {
    ONE,
    TWO
  }
}
