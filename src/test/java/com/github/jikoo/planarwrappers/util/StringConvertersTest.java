package com.github.jikoo.planarwrappers.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.when;

import com.github.jikoo.planarwrappers.mock.ServerMocks;
import com.github.jikoo.planarwrappers.mock.inventory.EnchantmentMocks;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
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
    when(server.getRegistry(notNull())).thenReturn(null);

    EnchantmentMocks.setEnchantments();
    Bukkit.setServer(server);
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
    Enchantment fromNamespaced = StringConverters.toEnchant(enchantment.getKey().toString());
    assertThat("Value must not be null", fromNamespaced, notNullValue());
    assertThat(
        "Value must be obtained from namespaced key.",
        fromNamespaced.getKey(),
        is(enchantment.getKey()));
    Enchantment fromUnNamespaced = StringConverters.toEnchant(enchantment.getKey().getKey());
    assertThat("Value must not be null", fromUnNamespaced, notNullValue());
    assertThat(
        "Value must be obtained from un-namespaced key.",
        fromUnNamespaced.getKey(),
        is(enchantment.getKey()));
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
        StringConverters.toMaterial(material.getKey().toString()),
        is(material));
    assertThat(
        "Value must be obtained from un-namespaced key.",
        StringConverters.toMaterial(material.getKey().getKey()),
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
