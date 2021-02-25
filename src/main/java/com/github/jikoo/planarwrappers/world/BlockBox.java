package com.github.jikoo.planarwrappers.world;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A mutable block-based axis-aligned bounding box.
 *
 * <p>This is a rectangular box defined by minimum and maximum corners that can be used to represent
 * a collection of blocks.
 *
 * <p>Unlike Bukkit's {@link BoundingBox}, this implementation is purely for whole blocks. This
 * means that degenerate boxes cannot exist - all sides have a minimum length of 1 block. In
 * addition, it is much more focused on performance and does not perform nearly as many useless
 * input sanitization operations.
 *
 * <p>Benchmarks for assembling a bounding box from a list of Blocks:
 *
 * <pre>
 * Benchmark          Mode  Cnt    Score   Error  Units
 * BlockBoundingBox   avgt   50   69.523 ± 0.358  ns/op
 * BukkitBoundingBox  avgt   50  320.323 ± 2.042  ns/op
 * ManualBoundingBox  avgt   50   58.978 ± 0.383  ns/op
 * </pre>
 *
 * Manually calculating bounding box contents is faster than using this class and allocates a tiny
 * bit less memory, but the decrease in maintainability and readability make the performance hit
 * worthwhile.
 */
public class BlockBox implements Cloneable {

  private int minX;
  private int minY;
  private int minZ;
  private int maxX;
  private int maxY;
  private int maxZ;

  /**
   * Construct a new bounding box with the given corners.
   *
   * @param x1 the X coordinate of the first corner
   * @param y1 the Y coordinate of the first corner
   * @param z1 the Z coordinate of the first corner
   * @param x2 the X coordinate of the second corner
   * @param y2 the Y coordinate of the second corner
   * @param z2 the Z coordinate of the second corner
   * @param verify whether or not to verify that the provided corners are in fact the minimum
   *     corners
   */
  protected BlockBox(int x1, int y1, int z1, int x2, int y2, int z2, boolean verify) {
    if (verify) {
      verify(x1, y1, z1, x2, y2, z2);
    } else {
      this.minX = x1;
      this.maxX = x2;
      this.minY = y1;
      this.maxY = y2;
      this.minZ = z1;
      this.maxZ = z2;
    }
  }

  /**
   * Construct a new bounding box with the given corners.
   *
   * @param x1 the X coordinate of the first corner
   * @param y1 the Y coordinate of the first corner
   * @param z1 the Z coordinate of the first corner
   * @param x2 the X coordinate of the second corner
   * @param y2 the Y coordinate of the second corner
   * @param z2 the Z coordinate of the second corner
   */
  public BlockBox(int x1, int y1, int z1, int x2, int y2, int z2) {
    this(x1, y1, z1, x2, y2, z2, true);
  }

  /**
   * Construct a new bounding box with the given corners.
   *
   * @param pos1 the position of the first corner
   * @param pos2 the position of the second corner
   * @param verify whether or not to verify that the provided corners are in fact the minimum
   *     corners
   */
  protected BlockBox(@NotNull Location pos1, @NotNull Location pos2, boolean verify) {
    this(
        pos1.getBlockX(),
        pos1.getBlockY(),
        pos1.getBlockZ(),
        pos2.getBlockX(),
        pos2.getBlockY(),
        pos2.getBlockZ(),
        verify);
  }

  /**
   * Construct a new bounding box with the given corners.
   *
   * @param pos1 the position of the first corner
   * @param pos2 the position of the second corner
   */
  public BlockBox(@NotNull Location pos1, @NotNull Location pos2) {
    this(pos1, pos2, true);
  }

  /**
   * Construct a new bounding box with the given corners.
   *
   * @param pos1 the position of the first corner
   * @param pos2 the position of the second corner
   */
  public BlockBox(@NotNull Vector pos1, @NotNull Vector pos2) {
    this(
        pos1.getBlockX(),
        pos1.getBlockY(),
        pos1.getBlockZ(),
        pos2.getBlockX(),
        pos2.getBlockY(),
        pos2.getBlockZ(),
        true);
  }

  /**
   * Construct a new bounding box representing the given block.
   *
   * @param block the block
   */
  public BlockBox(@NotNull Block block) {
    this(block.getX(), block.getY(), block.getZ(), block.getX(), block.getY(), block.getZ(), false);
  }

  /**
   * Construct a new bounding box representing the given Bukkit {@link BoundingBox}.
   *
   * @param boundingBox the Bukkit bounding box
   */
  public BlockBox(@NotNull BoundingBox boundingBox) {
    this(
        (int) boundingBox.getMinX(),
        (int) boundingBox.getMinY(),
        (int) boundingBox.getMinZ(),
        // Since Bukkit bounding boxes are inclusive of upper bounds, subtract a small number.
        // This ensures that a full block Bukkit bounding boxes yield correct equivalents.
        // Uses Math.max to account for degenerate boxes.
        (int) Math.max(boundingBox.getMinX(), boundingBox.getMaxX() - .0001),
        (int) Math.max(boundingBox.getMinY(), boundingBox.getMaxY() - .0001),
        (int) Math.max(boundingBox.getMinZ(), boundingBox.getMaxZ() - .0001),
        false);
  }

  /**
   * Construct a new bounding box containing all of the given blocks.
   *
   * @param blocks a collection of blocks to construct a bounding box around
   * @return the bounding box
   */
  public static @NotNull BlockBox ofBlocks(@NotNull Collection<Block> blocks) {
    if (blocks.size() == 0)
      throw new IllegalArgumentException("Cannot create bounding box with no blocks!");

    Iterator<Block> iterator = blocks.iterator();
    // Initialize bounding box with first block
    BlockBox box = new BlockBox(iterator.next());

    // Fill in rest of bounding box with remaining blocks.
    while (iterator.hasNext()) {
      box.union(iterator.next());
    }

    return box;
  }

  /**
   * Set bounds of this bounding box to the specified values. Ensures that the minimum and maximum
   * corners are set from the correct respective values.
   *
   * @param x1 the first X value
   * @param y1 the first Y value
   * @param z1 the first Z value
   * @param x2 the second X value
   * @param y2 the second Y value
   * @param z2 the second Z value
   */
  private void verify(int x1, int y1, int z1, int x2, int y2, int z2) {
    if (x1 < x2) {
      this.minX = x1;
      this.maxX = x2;
    } else {
      this.minX = x2;
      this.maxX = x1;
    }
    if (y1 < y2) {
      this.minY = y1;
      this.maxY = y2;
    } else {
      this.minY = y2;
      this.maxY = y1;
    }
    if (z1 < z2) {
      this.minZ = z1;
      this.maxZ = z2;
    } else {
      this.minZ = z2;
      this.maxZ = z1;
    }
  }

  /**
   * Get the minimum X coordinate of the bounding box.
   *
   * @return the minimum X value
   */
  public int getMinX() {
    return this.minX;
  }

  /**
   * Get the minimum Y coordinate of the bounding box.
   *
   * @return the minimum Y value
   */
  public int getMinY() {
    return this.minY;
  }

  /**
   * Get the minimum Y coordinate of the bounding box.
   *
   * @return the minimum Y value
   */
  public int getMinZ() {
    return this.minZ;
  }

  /**
   * Get the minimum corner's coordinates as a vector.
   *
   * @return the minimum corner as a vector
   */
  public @NotNull Vector getMin() {
    return new Vector(this.minX, this.minY, this.minZ);
  }

  /**
   * Get the maximum X coordinate of the bounding box.
   *
   * @return the maximum X value
   */
  public int getMaxX() {
    return this.maxX;
  }

  /**
   * Get the maximum Y coordinate of the bounding box.
   *
   * @return the maximum Y value
   */
  public int getMaxY() {
    return this.maxY;
  }

  /**
   * Get the maximum Z coordinate of the bounding box.
   *
   * @return the maximum Z value
   */
  public int getMaxZ() {
    return this.maxZ;
  }

  /**
   * Get the maximum corner's coordinates as a vector.
   *
   * @return the maximum corner as a vector
   */
  public @NotNull Vector getMax() {
    return new Vector(this.maxX, this.maxY, this.maxZ);
  }

  /**
   * Get the length of the bounding box on the X axis.
   *
   * @return the length on the X axis
   */
  public int getLength() {
    return (this.maxX - this.minX) + 1;
  }

  /**
   * Get the length of the bounding box on the Y axis.
   *
   * @return the length on the Y axis
   */
  public int getHeight() {
    return (this.maxY - this.minY) + 1;
  }

  /**
   * Get the length of the bounding box on the Z axis.
   *
   * @return the length on the Z axis
   */
  public int getWidth() {
    return (this.maxZ - this.minZ) + 1;
  }

  /**
   * Get the center of the bounding box on the X axis.
   *
   * <p>Note that center coordinates are world coordinates while all of the other coordinates are
   * block coordinates.
   *
   * @return the center of the X axis
   */
  public double getCenterX() {
    return this.minX + (this.getLength() / 2D);
  }

  /**
   * Get the center of the bounding box on the Y axis.
   *
   * <p>Note that center coordinates are world coordinates while all of the other coordinates are
   * block coordinates.
   *
   * @return the center of the X axis
   */
  public double getCenterY() {
    return this.minY + (this.getHeight() / 2D);
  }

  /**
   * Get the center of the bounding box on the Z axis.
   *
   * <p>Note that center coordinates are world coordinates while all of the other coordinates are
   * block coordinates.
   *
   * @return the center of the X axis
   */
  public double getCenterZ() {
    return this.minZ + (this.getWidth() / 2D);
  }

  /**
   * Get the center of the bounding box as a vector.
   *
   * <p>Note that center coordinates are world coordinates while all of the other coordinates are
   * block coordinates.
   *
   * @return the center of the X axis
   */
  public @NotNull Vector getCenter() {
    return new Vector(this.getCenterX(), this.getCenterY(), this.getCenterZ());
  }

  /**
   * Get the area of the base of the bounding box.
   *
   * <p>The base is the lowest plane defined by the X and Z axis.
   *
   * @return the area of the base of the bounding box
   */
  public int getArea() {
    return this.getLength() * this.getWidth();
  }

  /**
   * Get the volume of the bounding box.
   *
   * @return the volume of the bounding box
   */
  public int getVolume() {
    return this.getArea() * getHeight();
  }

  /**
   * Copy the dimensions and location of another bounding box.
   *
   * @param other the bounding box to copy
   */
  public void copy(@NotNull BlockBox other) {
    this.minX = other.minX;
    this.minY = other.minY;
    this.minZ = other.minZ;
    this.maxX = other.maxX;
    this.maxY = other.maxY;
    this.maxZ = other.maxZ;
  }

  /**
   * Change the size the bounding box in the direction specified by the Minecraft blockface.
   *
   * <p>If the specified directional magnitude is negative, the box is contracted instead.
   *
   * <p>When contracting, the box does not care if the contraction would cause a negative side
   * length. In these cases, the lowest point is redefined by the new location of the maximum corner
   * instead.
   *
   * @param direction the direction to change size in
   * @param magnitude the magnitude of the resizing
   */
  public void resize(@NotNull BlockFace direction, int magnitude) {
    if (magnitude == 0 || direction == BlockFace.SELF) {
      return;
    }

    Vector vector = direction.getDirection().multiply(magnitude);

    // Force normalized rounding - prevents issues with non-cardinal directions.
    int modX = NumberConversions.round(vector.getX());
    int modY = NumberConversions.round(vector.getY());
    int modZ = NumberConversions.round(vector.getZ());

    if (modX == 0 && modY == 0 && modZ == 0) return;

    // Modify correct point.
    if (direction.getModX() > 0) {
      this.maxX += modX;
    } else {
      this.minX += modX;
    }
    if (direction.getModY() > 0) {
      this.maxY += modY;
    } else {
      this.minY += modY;
    }
    if (direction.getModZ() > 0) {
      this.maxZ += modZ;
    } else {
      this.minZ += modZ;
    }

    // If box is contracting, re-verify points in case corners have swapped.
    if (magnitude < 0) verify(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
  }

  /**
   * Move the bounding box in the direction specified by the Minecraft BlockFace and magnitude.
   *
   * <p>Note that a negative direction will move in the opposite direction to the extent that the
   * following example returns true:
   *
   * <pre>
   * public boolean testBoxMove(BlockBox box, BlockFace face, int magnitude)
   * {
   *     BlockBox box2 = box.clone();
   *     box.move(face, magnitude);
   *     box2.move(face.getOpposite(), -magnitude);
   *     return box.equals(box2);
   * }
   * </pre>
   *
   * @param direction the direction to move in
   * @param magnitude the magnitude of the move
   */
  public void move(@NotNull BlockFace direction, int magnitude) {
    if (magnitude == 0 || direction == BlockFace.SELF) return;

    Vector vector = direction.getDirection().multiply(magnitude);

    int blockX = NumberConversions.round(vector.getX());
    this.minX += blockX;
    this.maxX += blockX;
    int blockY = NumberConversions.round(vector.getY());
    this.minY += blockY;
    this.maxY += blockY;
    int blockZ = NumberConversions.round(vector.getZ());
    this.minZ += blockZ;
    this.maxZ += blockZ;
  }

  /**
   * Expand the bounding box to contain the position specified.
   *
   * @param x the X coordinate to include
   * @param y the Y coordinate to include
   * @param z the Z coordinate to include
   */
  public void union(int x, int y, int z) {
    this.minX = Math.min(x, this.minX);
    this.maxX = Math.max(x, this.maxX);
    this.minY = Math.min(y, this.minY);
    this.maxY = Math.max(y, this.maxY);
    this.minZ = Math.min(z, this.minZ);
    this.maxZ = Math.max(z, this.maxZ);
  }

  /**
   * Expand the bounding box to contain the position specified.
   *
   * @param position the position to include
   */
  public void union(@NotNull Block position) {
    this.union(position.getX(), position.getY(), position.getZ());
  }

  /**
   * Expand the bounding box to contain the position specified.
   *
   * @param position the position to include
   */
  public void union(@NotNull Vector position) {
    this.union(position.getBlockX(), position.getBlockY(), position.getBlockZ());
  }

  /**
   * Expand the bounding box to contain the position specified.
   *
   * @param position the position to include
   */
  public void union(@NotNull Location position) {
    this.union(position.getBlockX(), position.getBlockY(), position.getBlockZ());
  }

  /**
   * Expand the bounding box to contain the bounding box specified.
   *
   * @param other the bounding box to include
   */
  public void union(@NotNull BlockBox other) {
    this.minX = Math.min(this.minX, other.minX);
    this.maxX = Math.max(this.maxX, other.maxX);
    this.minY = Math.min(this.minY, other.minY);
    this.maxY = Math.max(this.maxY, other.maxY);
    this.minZ = Math.min(this.minZ, other.minZ);
    this.maxZ = Math.max(this.maxZ, other.maxZ);
  }

  /**
   * Internal containment check ignoring vertical differences.
   *
   * @param minX the minimum X value to check for containment
   * @param minZ the minimum Z value to check for containment
   * @param maxX the maximum X value to check for containment
   * @param maxZ the maximum X value to check for containment
   * @return true if the specified values are inside the bounding box
   */
  private boolean contains2dInternal(int minX, int minZ, int maxX, int maxZ) {
    return minX >= this.minX && maxX <= this.maxX && minZ >= this.minZ && maxZ <= this.maxZ;
  }

  /**
   * Check if the bounding box contains the position specified.
   *
   * @param x the X coordinate of the position
   * @param z the Z coordinate of the position
   * @return true if the specified position is inside the bounding box
   */
  public boolean contains2d(int x, int z) {
    return contains2dInternal(x, z, x, z);
  }

  /**
   * Check if the bounding box contains the position specified.
   *
   * @param position the position
   * @return true if the specified position is inside the bounding box
   */
  public boolean contains2d(@NotNull Vector position) {
    return contains2d(position.getBlockX(), position.getBlockZ());
  }

  /**
   * Check if the bounding box contains the position specified.
   *
   * @param position the position
   * @return true if the specified position is inside the bounding box
   */
  public boolean contains2d(@NotNull Location position) {
    return contains2d(position.getBlockX(), position.getBlockZ());
  }

  /**
   * Check if the bounding box contains the position specified.
   *
   * @param position the position
   * @return true if the specified position is inside the bounding box
   */
  public boolean contains2d(@NotNull Block position) {
    return contains2d(position.getX(), position.getZ());
  }

  /**
   * Check if the bounding box contains another bounding box consisting of the positions specified.
   *
   * @param x1 the X coordinate of the first position
   * @param z1 the Z coordinate of the first position
   * @param x2 the X coordinate of the second position
   * @param z2 the Z coordinate of the second position
   * @return true if the specified positions are inside the bounding box
   */
  public boolean contains2d(int x1, int z1, int x2, int z2) {
    int minX;
    int maxX;
    if (x1 < x2) {
      minX = x1;
      maxX = x2;
    } else {
      minX = x2;
      maxX = x1;
    }
    int minZ;
    int maxZ;
    if (z1 < z2) {
      minZ = z1;
      maxZ = z2;
    } else {
      minZ = z2;
      maxZ = z1;
    }

    return contains2dInternal(minX, minZ, maxX, maxZ);
  }

  /**
   * Check if the bounding box contains another bounding box.
   *
   * @param other the other bounding box
   * @return true if the specified positions are inside the bounding box
   */
  public boolean contains2d(@NotNull BlockBox other) {
    return contains2dInternal(other.minX, other.minZ, other.maxX, other.maxZ);
  }

  /**
   * Internal containment check.
   *
   * @param minX the minimum X value to check for containment
   * @param minY the minimum Y value to check for containment
   * @param minZ the minimum Z value to check for containment
   * @param maxX the maximum X value to check for containment
   * @param maxY the maximum X value to check for containment
   * @param maxZ the maximum X value to check for containment
   * @return true if the specified values are inside the bounding box
   */
  private boolean containsInternal(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    return contains2dInternal(minX, minZ, maxX, maxZ) && minY >= this.minY && maxY <= this.maxY;
  }

  /**
   * Check if the bounding box contains the position specified.
   *
   * @param x the X coordinate of the position
   * @param y the Y coordinate of the position
   * @param z the Z coordinate of the position
   * @return true if the specified position is inside the bounding box
   */
  public boolean contains(int x, int y, int z) {
    return containsInternal(x, y, z, x, y, z);
  }

  /**
   * Check if the bounding box contains the position specified.
   *
   * @param position the position
   * @return true if the specified position is inside the bounding box
   */
  public boolean contains(@NotNull Vector position) {
    return contains(position.getBlockX(), position.getBlockY(), position.getBlockZ());
  }

  /**
   * Check if the bounding box contains the position specified.
   *
   * @param position the position
   * @return true if the specified position is inside the bounding box
   */
  public boolean contains(@NotNull Location position) {
    return contains(position.getBlockX(), position.getBlockY(), position.getBlockZ());
  }

  /**
   * Check if the bounding box contains the position specified.
   *
   * @param position the position
   * @return true if the specified position is inside the bounding box
   */
  public boolean contains(@NotNull Block position) {
    return contains(position.getX(), position.getY(), position.getZ());
  }

  /**
   * Check if the bounding box contains another bounding box consisting of the positions specified.
   *
   * @param x1 the X coordinate of the first position
   * @param y1 the Y coordinate of the first position
   * @param z1 the Z coordinate of the first position
   * @param x2 the X coordinate of the second position
   * @param y2 the Y coordinate of the second position
   * @param z2 the Z coordinate of the second position
   * @return true if the specified positions are inside the bounding box
   */
  public boolean contains(int x1, int y1, int z1, int x2, int y2, int z2) {
    return contains(new BlockBox(x1, y1, z1, x2, y2, z2));
  }

  /**
   * Check if the bounding box contains another bounding box.
   *
   * @param other the other bounding box
   * @return true if the specified positions are inside the bounding box
   */
  public boolean contains(@NotNull BlockBox other) {
    return containsInternal(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
  }

  /**
   * Check if the bounding box intersects another bounding box.
   *
   * @param other the other bounding box
   * @return true if the specified positions are inside the bounding box
   */
  public boolean intersects(@NotNull BlockBox other) {
    // For help visualizing test cases, try https://silentmatt.com/rectangle-intersection/
    return this.minX <= other.maxX
        && this.maxX >= other.minX
        && this.minY <= other.maxY
        && this.maxY >= other.minY
        && this.minZ <= other.maxZ
        && this.maxZ >= other.minZ;
  }

  /**
   * Get a bounding box containing the intersection of the bounding box with another.
   *
   * @param other the other bounding box
   * @return the bounding box representing overlapping area or null if the boxes do not overlap.
   */
  public @Nullable BlockBox intersection(@NotNull BlockBox other) {
    if (!intersects(other)) {
      return null;
    }

    return new BlockBox(
        Math.max(this.minX, other.minX),
        Math.max(this.minY, other.minY),
        Math.max(this.minZ, other.minZ),
        Math.min(this.maxX, other.maxX),
        Math.min(this.maxY, other.maxY),
        Math.min(this.maxZ, other.maxZ),
        false);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BlockBox)) {
      return false;
    }
    BlockBox other = (BlockBox) o;
    return this.minX == other.minX
        && this.minY == other.minY
        && this.minZ == other.minZ
        && this.maxX == other.maxX
        && this.maxY == other.maxY
        && this.maxZ == other.maxZ;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
  }

  @Override
  public @NotNull String toString() {
    return "BlockBox{"
        + "minX="
        + minX
        + ", minY="
        + minY
        + ", minZ="
        + minZ
        + ", maxX="
        + maxX
        + ", maxY="
        + maxY
        + ", maxZ="
        + maxZ
        + '}';
  }

  @Override
  public @NotNull BlockBox clone() {
    try {
      return (BlockBox) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new Error(e);
    }
  }
}
