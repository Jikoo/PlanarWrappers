package com.github.jikoo.planarwrappers.mock.inventory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

public final class EnchantmentMocks {

  public static void setEnchantments() {
    try {
      for (Field field : Enchantment.class.getDeclaredFields()) {
        if (field.getType().equals(Enchantment.class)) {
          NamespacedKey key = ((Enchantment) field.get(null)).getKey();
          Enchantment.registerEnchantment(EnchantmentMocks.getEnchantment(key));
        }
      }
      Enchantment.stopAcceptingRegistrations();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void tearDownEnchantments() {
    try {
      Field byNameField = Enchantment.class.getDeclaredField("byName");
      byNameField.setAccessible(true);
      Object byName = byNameField.get(null);

      Field byKeyField = Enchantment.class.getDeclaredField("byKey");
      byKeyField.setAccessible(true);
      Object byKey = byKeyField.get(null);

      Method clear = Map.class.getMethod("clear");
      clear.invoke(byName);
      clear.invoke(byKey);

      Field acceptingNew = Enchantment.class.getDeclaredField("acceptingNew");
      acceptingNew.setAccessible(true);
      acceptingNew.set(null, true);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  public static @NotNull Enchantment getEnchantment(@NotNull NamespacedKey key) {
    Enchantment mock = mock(Enchantment.class);
    when(mock.getKey()).thenReturn(key);
    try {
      Field keyField = Enchantment.class.getDeclaredField("key");
      keyField.setAccessible(true);
      keyField.set(mock, key);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    when(mock.getName()).thenReturn(key.getKey().toUpperCase());

    return mock;
  }

  private EnchantmentMocks() {}

}
