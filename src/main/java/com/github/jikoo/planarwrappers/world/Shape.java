package com.github.jikoo.planarwrappers.world;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Create rotatable multi-block shapes in a slightly more sane coordinate system.
 *
 * <p>This does not use Minecraft's coordinate system where north is negative Z. Instead, north is
 * positive Z and east is positive X. Unlike a traditional coordinate system, Y remains tied to
 * height instead of Z.
 *
 * <p>Shapes are rotatable in 90 degree increments around the Y axis.
 */
public class Shape {

  /** Relative vectors and block data */
  private final LinkedHashMap<Vector, TransformableBlockData> vectorData;

  /** Constructor for a new empty Shape.*/
  public Shape() {
    this.vectorData = new LinkedHashMap<>();
  }

  /**
   * Set transformable block data for a local location.
   *
   * @param vector the Vector representing a local location
   * @param data the TransformableBlockData
   */
  public void set(@NotNull Vector vector, @NotNull TransformableBlockData data) {
    vectorData.put(vector, data);
  }

  /**
   * Set transformable block data for a local location.
   *
   * @param x the local X coordinate
   * @param y the local Y coordinate
   * @param z the local Z coordinate
   * @param data the TransformableBlockData
   */
  public void set(int x, int y, int z, @NotNull TransformableBlockData data) {
    set(new Vector(x, y, z), data);
  }

  /**
   * Set block type for a local location.
   *
   * @param x the local X coordinate
   * @param y the local Y coordinate
   * @param z the local Z coordinate
   * @param data the block type
   */
  public void set(int x, int y, int z, @NotNull Material data) {
    if (!data.isBlock()) {
      throw new IllegalArgumentException(String.format("Material %s is not a block", data.name()));
    }
    set(x, y, z, new TransformableBlockData(data));
  }

  /**
   * Get all properly oriented Blocks and BlockData needed to build the Shape.
   *
   * @param key the Block to center the Shape on
   * @param direction the Direction the Shape should be rotated
   * @return the Blocks and relative BlockData
   */
  public Map<Block, BlockData> getBuildLocations(@NotNull Block key, @NotNull Direction direction) {
    Map<Block, BlockData> newLocs = new HashMap<>();
    for (Entry<Vector, TransformableBlockData> entry : vectorData.entrySet()) {
      Vector relativeVector = getRelativeVector(direction, entry.getKey());
      newLocs.put(
          key.getRelative(
              relativeVector.getBlockX(), relativeVector.getBlockY(), relativeVector.getBlockZ()),
              entry.getValue().getTransformedData(direction)
          );
    }
    return newLocs;
  }

  /**
   * Assemble the Shape at the given key block.
   *
   * @param key the Block to center the Shape on
   * @param direction the Direction the Shape should be rotated
   */
  public void build(@NotNull Block key, @NotNull Direction direction) {
    for (Entry<Block, BlockData> entry :
        this.getBuildLocations(key, direction).entrySet()) {
      entry.getKey().setBlockData(entry.getValue());
    }
  }

  /**
   * Get a Vector translated from the internal representation.
   *
   * @param direction the Direction
   * @param vector the Vector to translate
   * @return the new Vector
   */
  private static Vector getRelativeVector(@NotNull Direction direction, @NotNull Vector vector) {
    vector = vector.clone();
    switch (direction) {
      case EAST:
        double newZ = vector.getX();
        vector.setX(vector.getZ());
        vector.setZ(newZ);
        return vector;
      case SOUTH:
        vector.setX(-2 * vector.getBlockX() + vector.getX());
        int blockZ = (int) vector.getZ();
        vector.setZ(blockZ + vector.getZ() - blockZ);
        return vector;
      case WEST:
        double newZ1 = -2 * vector.getBlockX() + vector.getX();
        vector.setX(-2 * vector.getBlockZ() + vector.getZ());
        vector.setZ(newZ1);
        return vector;
      case NORTH:
      default:
        vector.setZ(-2 * vector.getBlockZ() + vector.getZ());
        return vector;
    }
  }
}
