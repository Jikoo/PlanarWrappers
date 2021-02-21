package com.github.jikoo.planarwrappers.collections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;

/**
 * A utility for creating FastUtil maps.
 *
 * <p>While Craftbukkit and Paper both shade FastUtil, CB relocates FastUtil and Paper does not. The
 * performance gains of using FastUtil's maps are worth the hit of fumbling around a bit initially
 * to figure out which is present.
 */
@SuppressWarnings("unchecked")
class FastMap {

  private static Class<Map<?, ?>> obj2Obj;
  private static Class<SortedMap<Integer, ?>> int2ObjTree;
  private static Class<Map<Integer, ?>> int2Obj;

  private FastMap() {}

  /**
   * Create an {@code Object2ObjectOpenHashMap} if the class can be found.
   *
   * <p>If FastUtil cannot be located, provides a {@code HashMap} instead.
   *
   * @param <K> the type of key
   * @param <V> the type of value
   * @return the Map created
   */
  static <K, V> Map<K, V> obj2Obj() {
    if (obj2Obj == null) {
      obj2Obj = newFast("it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap", HashMap.class);
    }

    return (Map<K, V>) instanceOr(obj2Obj, HashMap::new);
  }

  /**
   * Create an {@code Int2ObjectOpenHashMap} if the class can be found.
   *
   * <p>If FastUtil cannot be located, provides a {@code HashMap} instead.
   *
   * @param <V> the type of value
   * @return the Map created
   */
  static <V> Map<Integer, V> int2Obj() {
    if (int2Obj == null) {
      int2Obj = newFast("it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap", HashMap.class);
    }

    return (Map<Integer, V>) instanceOr(int2Obj, HashMap::new);
  }

  /**
   * Create an {@code Int2ObjectRBTreeMap} if the class can be found.
   *
   * <p>If FastUtil cannot be located, provides a {@code HashMap} instead.
   *
   * @param <V> the type of value
   * @return the Map created
   */
  static <V> SortedMap<Integer, V> int2ObjTree() {
    if (int2ObjTree == null) {
      int2ObjTree = newFast("it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap", TreeMap.class);
    }

    return (SortedMap<Integer, V>) instanceOr(int2ObjTree, TreeMap::new);
  }

  private static <T extends Map<?, ?>, V extends T> Class<T> newFast(
      String className, Class<V> defaultClazz) {
    if (Modifier.isAbstract(defaultClazz.getModifiers())) {
      throw new IllegalArgumentException(
          "Cannot provide a default implementation that cannot be instantiated!");
    }
    try {
      // CB/Spigot provides FastUtil but relocates it.
      return (Class<T>) Class.forName("org.bukkit.craftbukkit.libs." + className);
    } catch (ClassNotFoundException e) {
      // Not relocated by CB, move on.
    }
    try {
      // Paper removes FastUtil relocation.
      return (Class<T>) Class.forName(className);
    } catch (ClassNotFoundException e) {
      // Not present at all? Fall through to provided implementation.
    }
    // Hack around compilation error
    return (Class<T>) defaultClazz;
  }

  private static <T> T instanceOr(Class<? extends T> clazz, Supplier<T> supplier) {
    try {
      return clazz.getConstructor().newInstance();
    } catch (InstantiationException
        | InvocationTargetException
        | IllegalAccessException
        | NoSuchMethodException e) {
      // Fall through to supplier.
      return supplier.get();
    }
  }
}
