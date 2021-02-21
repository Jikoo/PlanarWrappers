package com.github.jikoo.planarwrappers.collections;

import com.github.jikoo.planarwrappers.util.Coords;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A more performant expression of a {@code Map<Block, ?>}. Also supports chunk-based operations.
 *
 * @param <V> the type of value stored
 */
public class BlockMap<V> {

  private final Map<String, SortedMap<Integer, SortedMap<Integer, Map<Integer, V>>>> serverMap =
      FastMap.obj2Obj();

  /**
   * Add a mapping for a {@link Block}.
   *
   * @param block the key used to map the value
   * @param value the value to be stored using the key
   * @return the previously stored value or {@code null} if no value was present
   */
  public @Nullable V put(@NotNull Block block, @Nullable V value) {
    SortedMap<Integer, SortedMap<Integer, Map<Integer, V>>> worldMap =
        serverMap.computeIfAbsent(block.getWorld().getName(), k -> FastMap.int2ObjTree());
    SortedMap<Integer, Map<Integer, V>> blockXMap =
        worldMap.computeIfAbsent(block.getX(), k -> FastMap.int2ObjTree());
    Map<Integer, V> blockZMap =
        blockXMap.computeIfAbsent(block.getZ(), blockZ -> FastMap.int2Obj());

    return blockZMap.put(block.getY(), value);
  }

  /**
   * Get a stored mapping for a {@link Block}.
   *
   * @param block the key used to map the value
   * @return the stored value or {@code null} if no value is present
   */
  public @Nullable V get(@NotNull Block block) {
    SortedMap<Integer, SortedMap<Integer, Map<Integer, V>>> xzyMap =
        serverMap.get(block.getWorld().getName());
    if (xzyMap == null) {
      return null;
    }

    SortedMap<Integer, Map<Integer, V>> zyMap = xzyMap.get(block.getX());
    if (zyMap == null) {
      return null;
    }

    Map<Integer, V> yMap = zyMap.get(block.getZ());
    if (yMap == null) {
      return null;
    }

    return yMap.get(block.getY());
  }

  /**
   * Get all stored mappings for a {@link Chunk}.
   *
   * @param chunk the {@code Chunk} of blocks to retrieve
   * @return the stored values
   */
  public @NotNull Collection<V> get(@NotNull Chunk chunk) {
    return get(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
  }

  /**
   * Get all stored mappings for the chunk coordinates in the world named.
   *
   * <p>Note that this method accepts {@link Chunk} coordinates, not block coordinates! If
   * necessary, convert with the {@link Coords} utility.
   *
   * @param world the name of the world
   * @param chunkX the chunk X coordinate
   * @param chunkZ the chunk Z coordinate
   * @return the stored values
   */
  public @NotNull Collection<V> get(@NotNull String world, int chunkX, int chunkZ) {
    SortedMap<Integer, SortedMap<Integer, Map<Integer, V>>> xzyMap = serverMap.get(world);
    if (xzyMap == null) {
      return Collections.emptyList();
    }

    int blockXMin = Coords.chunkToBlock(chunkX);
    // Submap fetching is high boundary exclusive, shift responsibility for chunk size to Coords.
    int blockXMax = Coords.chunkToBlock(chunkX + 1);
    SortedMap<Integer, SortedMap<Integer, Map<Integer, V>>> zySubMap =
        xzyMap.subMap(blockXMin, blockXMax);
    if (zySubMap.isEmpty()) {
      return Collections.emptyList();
    }

    List<V> values = new ArrayList<>();
    int blockZMin = Coords.chunkToBlock(chunkZ);
    int blockZMax = Coords.chunkToBlock(chunkZ + 1);
    for (Map.Entry<Integer, SortedMap<Integer, Map<Integer, V>>> zyEntry : zySubMap.entrySet()) {
      SortedMap<Integer, Map<Integer, V>> ySubMap = zyEntry.getValue().subMap(blockZMin, blockZMax);

      for (Map<Integer, V> blockYMap : ySubMap.values()) {
        values.addAll(blockYMap.values());
      }
    }

    return values;
  }

  /**
   * Remove a mapping for a {@link Block}.
   *
   * @param block the key used to map the value
   * @return the previously stored value or {@code null} if no value was present
   */
  public @Nullable V remove(@NotNull Block block) {
    SortedMap<Integer, SortedMap<Integer, Map<Integer, V>>> xzyMap =
        serverMap.get(block.getWorld().getName());
    if (xzyMap == null) {
      return null;
    }

    SortedMap<Integer, Map<Integer, V>> zyMap = xzyMap.get(block.getX());
    if (zyMap == null) {
      return null;
    }

    Map<Integer, V> yMap = zyMap.get(block.getZ());
    if (yMap == null) {
      return null;
    }

    return yMap.remove(block.getY());
  }

  /**
   * Remove all stored mappings for a {@link Chunk}.
   *
   * @param chunk the {@code Chunk} of blocks to remove
   * @return the previously stored values
   */
  public @NotNull Collection<V> remove(@NotNull Chunk chunk) {
    return remove(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
  }

  /**
   * Remove all stored mappings for the chunk coordinates in the world named.
   *
   * <p>Note that this method accepts {@link Chunk} coordinates, not block coordinates! If
   * necessary, convert with the {@link Coords} utility.
   *
   * @param world the name of the world
   * @param chunkX the chunk X coordinate
   * @param chunkZ the chunk Z coordinate
   * @return the previously stored values
   */
  public @NotNull Collection<V> remove(@NotNull String world, int chunkX, int chunkZ) {
    SortedMap<Integer, SortedMap<Integer, Map<Integer, V>>> xzyMap = serverMap.get(world);
    if (xzyMap == null) {
      return Collections.emptyList();
    }

    int blockXMin = Coords.chunkToBlock(chunkX);
    SortedMap<Integer, SortedMap<Integer, Map<Integer, V>>> zySubMap =
        xzyMap.subMap(blockXMin, blockXMin + 16);
    if (zySubMap.isEmpty()) {
      return Collections.emptyList();
    }

    List<V> values = new ArrayList<>();
    int blockZMin = Coords.chunkToBlock(chunkZ);

    for (Iterator<SortedMap<Integer, Map<Integer, V>>> blockXIterator =
            zySubMap.values().iterator();
        blockXIterator.hasNext(); ) {
      SortedMap<Integer, Map<Integer, V>> zyValue = blockXIterator.next();
      SortedMap<Integer, Map<Integer, V>> ySubMap = zyValue.subMap(blockZMin, blockZMin + 16);

      for (Iterator<Map<Integer, V>> yIterator = ySubMap.values().iterator();
          yIterator.hasNext(); ) {
        values.addAll(yIterator.next().values());
        yIterator.remove();
      }

      if (zyValue.isEmpty()) {
        blockXIterator.remove();
      }
    }

    return values;
  }

  /**
   * Gets a collection of entries.
   *
   * <p>N.B. This ignores any entries that do not currently have a loaded world!
   *
   * @return a collection of entries
   */
  public @NotNull Collection<Map.Entry<Block, V>> entrySet() {
    List<Map.Entry<Block, V>> entries = new ArrayList<>();

    this.serverMap.forEach(
        (worldName, worldMap) -> {
          World world = Bukkit.getWorld(worldName);
          if (world == null) {
            return;
          }

          worldMap.forEach(
              (x, xMap) ->
                  xMap.forEach(
                      (z, zMap) ->
                          zMap.forEach(
                              (y, value) -> {
                                  Block key = world.getBlockAt(x, y, z);
                                  entries.add(
                                      new Map.Entry<Block, V>() {
                                        @Override
                                        public Block getKey() {
                                          return key;
                                        }

                                        @Override
                                        public V getValue() {
                                          return value;
                                        }

                                        @Override
                                        public V setValue(Object value) {
                                          throw new UnsupportedOperationException(
                                              "Modification of mappings not allowed here!");
                                        }

                                        @Override
                                        public boolean equals(Object obj) {
                                          if (!(obj instanceof Map.Entry)) {
                                            return false;
                                          }
                                          Entry<?, ?> other = (Entry<?, ?>) obj;
                                          return key.equals(other.getKey()) && value.equals(other.getValue());
                                        }

                                        @Override
                                        public int hashCode() {
                                          return Objects.hash(key, value);
                                        }
                                      });
                              })));
        });

    return entries;
  }

}
