package com.github.jikoo.planarwrappers.collections;

import com.github.jikoo.planarwrappers.collections.BlockMap.BlockMapEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A more performant expression of a {@code Map<Block, <List<V>>}. Also supports chunk-based
 * operations.
 *
 * @param <V> the type of value stored
 */
public class BlockMultimap<V> {

  private final BlockMap<List<V>> blockMap = new BlockMap<>();

  /**
   * Add a mapping for a {@link Block}.
   *
   * @param block the key used to map the value
   * @param value the value to be stored using the key
   */
  public void put(@NotNull Block block, @NotNull V value) {
    List<V> list = blockMap.get(block);
    if (list == null) {
      list = new ArrayList<>();
      blockMap.put(block, list);
    }
    list.add(value);
  }

  /**
   * Get all mappings for a {@link Block}.
   *
   * <p>Note that the Collection returned is immutable.
   *
   * @param block the key used to map the value
   * @return the values stored using the key, or {@code null} if no values have been set
   */
  public @Nullable Collection<V> get(@NotNull Block block) {
    List<V> list = blockMap.get(block);
    if (list == null) {
      return null;
    }
    return Collections.unmodifiableCollection(list);
  }

  /**
   * Get all mappings for a {@link Chunk}.
   *
   * <p>Note that the Collection returned is immutable.
   *
   * @param chunk the chunk of block keys
   * @return the values associated with the keys
   */
  public @NotNull Collection<V> get(@NotNull Chunk chunk) {
    return get(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
  }

  /**
   * Get all mappings for a {@link Chunk}.
   *
   * <p>Note that the Collection returned is immutable.
   *
   * @param world the world name
   * @param chunkX the chunk X coordinate
   * @param chunkZ the chunk Z coordinate
   * @return the values associated with the keys
   */
  public @NotNull Collection<V> get(@NotNull String world, int chunkX, int chunkZ) {
    return Collections.unmodifiableCollection(
        blockMap.get(world, chunkX, chunkZ).stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList()));
  }

  /**
   * Remove all mappings for a {@link Block}.
   *
   * <p>Note that the Collection returned is immutable.
   *
   * @param block the key used to map the value
   * @return the values stored using the key, or {@code null} if no values have been set
   */
  public @Nullable Collection<V> remove(@NotNull Block block) {
    List<V> list = blockMap.remove(block);
    if (list == null) {
      return null;
    }
    return Collections.unmodifiableCollection(list);
  }

  /**
   * Remove all mappings for a {@link Chunk}.
   *
   * <p>Note that the Collection returned is immutable.
   *
   * @param chunk the chunk of block keys
   * @return the values associated with the keys
   */
  public @NotNull Collection<V> remove(@NotNull Chunk chunk) {
    return remove(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
  }

  /**
   * Remove all mappings for a {@link Chunk}.
   *
   * <p>Note that the Collection returned is immutable.
   *
   * @param world the world name
   * @param chunkX the chunk X coordinate
   * @param chunkZ the chunk Z coordinate
   * @return the values associated with the keys
   */
  public @NotNull Collection<V> remove(@NotNull String world, int chunkX, int chunkZ) {
    return Collections.unmodifiableCollection(
        blockMap.remove(world, chunkX, chunkZ).stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList()));
  }

  public Collection<Map.Entry<Block, Collection<V>>> entrySet() {
    return Collections.unmodifiableCollection(
        blockMap.entrySet().stream()
            .map(
                entry ->
                    new BlockMapEntry<>(
                        entry.getKey(), Collections.unmodifiableCollection(entry.getValue())))
            .collect(Collectors.toList()));
  }
}
