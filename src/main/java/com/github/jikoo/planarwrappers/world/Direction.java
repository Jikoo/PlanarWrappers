package com.github.jikoo.planarwrappers.world;

import java.util.Locale;
import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Enum for compass direction based on Player yaw.
 *
 * <p>Because {@link Shape Shapes} only support rotation around the Y axis, there is no concept of
 * up or down. Any {@link org.bukkit.block.data.BlockData BlockData} details requiring up or down
 * values should be set before creating a {@link TransformableBlockData}.
 * <br>For example, creating a fire burning two blocks as part of a {@code Shape}:
 * <pre>
 *   Shape shape = new Shape();
 *   // Establish blocks to be burned.
 *   shape.set(0, 1, 0, Material.OAK_PLANKS); // Block "up"
 *   shape.set(0, 0, 1, Material.OAK_PLANKS); // Block "north"
 *   // Create block data.
 *   BlockData data = Bukkit.createBlockData(Material.FIRE);
 *
 *   if (data instanceof Fire) {
 *     Fire fire = (Fire) data;
 *     // Set upward face
 *     fire.setFace(BlockFace.UP, true);
 *   }
 *
 *   TransformableBlockData transformable = new TransformableBlockData(data);
 *   // Transform fire to face block "north" of itself in Shape.
 *   transformable.withTransformer(new MultipleFacingTransformer(EnumSet.of(Direction.NORTH)));
 *   shape.set(0, 0, 0, transformable);
 * </pre>
 */
public enum Direction {

  NORTH,
  EAST,
  SOUTH,
  WEST;

  /**
   * Get a {@code Direction} based on yaw.
   *
   * @param player the {@link Player}
   * @return the {@code Direction}
   */
  public static Direction getFacingDirection(Player player) {
    return getFacingDirection(player.getLocation());
  }

  /**
   * Get a {@code Direction} based on yaw.
   *
   * @param location the {@link Location}
   * @return the {@code Direction}
   */
  public static Direction getFacingDirection(Location location) {
    // Divide by 90 degree increments (360 degrees / 4 segments)
    float yaw = location.getYaw() / 90;
    // Round using Bukkit's negative-coordinate-safe utility.
    // Mask with 11 in binary, 3, to ignore insignificant over/underflow
    // Similar to %= 4 but handles negatives properly.
    byte facing = (byte) (NumberConversions.round(yaw) & 0b11);
    switch (facing) {
      case 0:
        return SOUTH;
      case 1:
        return WEST;
      case 3:
        return EAST;
      case 2:
      default:
        return NORTH;
    }
  }

  /**
   * A safe version of {@link #valueOf(String)}.
   *
   * @param directionName the name of the direction
   * @return the value or {@code NORTH} if the direction cannot be parsed
   */
  public static Direction safeValue(@Nullable String directionName) {
    if (directionName == null) {
      return NORTH;
    }
    try {
      return Enum.valueOf(Direction.class, directionName.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      return NORTH;
    }
  }

  /**
   * Get the corresponding {@link BlockFace}.
   *
   * @return the {@code BlockFace}
   */
  public BlockFace toBlockFace() {
    switch (this) {
      case EAST:
        return BlockFace.EAST;
      case SOUTH:
        return BlockFace.SOUTH;
      case WEST:
        return BlockFace.WEST;
      case NORTH:
      default:
        return BlockFace.NORTH;
    }
  }

  /**
   * Get the corresponding {@link Axis}.
   *
   * @return the {@code Axis}
   */
  public Axis toAxis() {
    switch (this) {
      case EAST:
      case WEST:
        return Axis.X;
      case NORTH:
      case SOUTH:
      default:
        return Axis.Z;
    }
  }

  /**
   * For obtaining rotation based on original direction.
   *
   * <p>The input {@code Direction} is the desired rotation for blocks relative to the original
   * {@code Direction}. The original {@code Direction} is treated as north relative to the new
   * {@code Direction}, as all {@link Shape Shapes} are designed from a north-facing perspective.
   *
   * <p>Ex.: A block in a {@code Shape} faces east by default. When the {@code Shape} is placed
   * westward, the block will face north.
   *
   * @param direction the {@code Direction}
   */
  public Direction getRelativeDirection(Direction direction) {
    return values()[(this.ordinal() + direction.ordinal()) % 4];
  }

  /**
   * Get a Vector translated from the internal representation.
   *
   * @param vector the Vector to translate
   * @return the new Vector
   */
  public Vector getRelativeVector(@NotNull Vector vector) {
    vector = vector.clone();
    switch (this) {
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
